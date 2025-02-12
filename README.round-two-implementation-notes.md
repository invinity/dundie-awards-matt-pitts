## 1. Implement the Endpoint 
> Create the endpoint "/give-dundie-awards/{organizationId}". This endpoint should increase the number of Dundie awards for each employee in the specified organization by 1. Ensure that any related updates are also handled appropriately. 

- Implementing this endpoint as-is would have been more JSON-RPC style design than REST, since this is essentally a *verb* as a URL. Good REST design is typically to have URLs be *nouns* representing an actual *resource*. Mutating activities we perform on these resources would be done using the various HTTP methods (POST, PUT, DELETE). 
- For this reason, I implemented this as a *POST* request to `/organization/{id}/employees`. This URL represents the *resource* that is "all employees in a given organization". The *POST* request itself will represent a `OrganizationEmployeeAction` that is to be performed on all employees of that `Organization`.
- For the specific use-case of *giving Dundie awards to all employees of an organization*, there is an action type of `GiveDundieAwardsAction`, which when *POST*ed to this REST endpoint will perform the action to give the awards.
## 2. Complete Additional Improvements 
> Address any additional improvements discussed during the call, including those you identified yourself. Please be mindful of your time—focus on changes that are manageable within the given timeframe. 
- Fixed `AwardsCache` to use `AtomicLong` for proper concurrent access and to avoid integer-overrun
- Added working StringDoc and Swagger UI page
- Added SpringDoc `@Operation` annotations to detail REST semantics and generate accurate openapi spec
- Added tests to >60% coverage
- Added javadoc in many places
- Added lombok and reduced boilerplate code
- Replaced field-based spring dependency injection with constructor-based; using `final` fields
- Centralized business logic into `@Service` classes
- `EmployeeController` now only requires a single injected dependency
## 3. Finish Message Broker Implementation 
> Complete the implementation of the Message Broker by either introducing a library or creating a basic publish/subscribe mechanism. 
- Applied the use of JMS via the `@EnableJms` Spring annotation
- No specific `MessageBroker` is needed now, as we can use the native JMS/Spring APIs for message pub/sub
    - Removed `MessageBroker` for this reason
## 4. Asynchronous Activity Creation 
> Implement the creation of an Activity when awards are added to an organization. This should be done asynchronously by subscribing to notifications from the Message Broker. 
- This was implemented using a method annotated with `@JmsListener` leveraging reliable JMS messaging
## 5. Implement Rollback Mechanism 
>Develop a mechanism to roll back the award distribution if the Activity creation fails.
### This part is tricky :-)
- Because `Activity` entities are to be recorded asynchronously from the update to give awards, there is no way to leverage a *true*, *ACID*-compliant database rollback
- Spring's `@Transactional` and `@Async` capabilities might seem applicable here, however, transaction contexts don't pass into asynchronous calls. So I don't see this working correctly to meet the requirements.

#### A "Ledger" Implementation?
- The only implementation that comes to mind for me would be some form of a change "ledger" where the activity of giving the awards is itself recorded somewhere
- This record would have to contain enough meta-data about what was actually changed to have an accurate form of *reversal* of the activity
- This is still not a true transaction rollback, and would be more of a "best effort" approach
- An implementation like this introduces a good bit of complexity that might not warrant the ROI of building/maintaining it
    - An edge-case example: What if `Employee` records originally affected by the award-giving are deleted or modified in between the time that the original change happened and when this attempt to roll-back occurs?
    - This ledger will itself require some form of persistent storage, which could also fail.
        - This essentially creates the same problem of having additional persistence in the original request-processing thread
#### A Practical Interpretation
- I break this down into a few specialized requirements here:
    1. A *Functional Requirement (FR)* that there is some record of the original award-giving activity (e.g. an audit log)
    1. A *FR* that this audit-log recording be done into the actual database and not some other form of audit-logging (based on the use of the `Activity` entity)
        - This could be implemented in another way altogether like structured logging to a centralized system
    1. A *Non-Functional Requirement (NFR)* that the writing of this `Activity` record to the database be done asynchronously to avoid any issue with this database activity interrupting or impacting the activity to respond to the original award-giving request
- There are established patterns for accomplishing this type of thing in a reliable way
    - Namely the use of a reliable message-queue system that is written to as part of the original transaction
    - A *JTA* `TransactionManager` implementation can be added for true multi-system transaction control and rollback
    - This allows for true and proper database rollback in the event that the message can not be delivered
    - Reliable message queues are purpose-built for this use-case to allow for very fast, but reliable message delivery with least impact to the original thread publishing the message
    - We have a guarantee from the messaging application that the message is delivered and will survive an outage
    - We can then implement a subscriber to these messages that writes the `Activity` database records appropriately
### How I accomplished the above NF and NFR
1. Added *JMS* capabilities to the springboot application
1. Added *JTA* capabilities to the springboot application
    - Used the *Atomikos* implementation
1. Centralized the award-giving business logic into a new spring `@Service` called `GiveDundieAwardsService`
1. This Service has a method `giveDundieAwardsByOrganization` that:
    1. Uses dependencies:
        - `EmployeeRepository`
        - `JmsTemplate`
    1. And performs *both* of the following in a single, JTA transaction:
        1. The award-giving update across all `Employee`s in a given `Organization`
        1. Inserts a message into the message-queue indicating this activity took place
            - The message is a serializable object type carrying meta information about the award-giving activity
1. `GiveDundieAwardsService` also has a method annotated with `@JmsListener` that will be called *asynchronously* when one of the messages published from `giveDundieAwardsByOrganization` comes through the message queue
    - This method, called `receiveDundieMessage`, will create the necessary `Activity` instance and save it in the database using the `ActivityRepository`
    - It will also increment the `AwardsCache` counter as appropriate
    - I added fields to `Actvity` capture the `Thread`s in which the original audit-log activity was created (e.g. `occurredInThread`) and where the resulting JMS message was processed into an `Activity` entity (e.g. `createdInThread`)
1. `IndexController` and `index.html` were also updated to show these new message and activity mechanisms
#### Notes
- Because the award-giving database activity *AND* the JMS message delivery occur in the same, logic transaction then a `RuntimeException` in either prevents both from succeeding.
- Because this implementation uses a reliable message-queue, it can be configured with additional semantics for retries, etc. to ensure reliable creation of the `Activity` database entry *without* doing it in the original request-handling thread
- Kafka might be a more preferred message-queue system for this type of thing, but using *JMS* simplified the changes for this assignment
