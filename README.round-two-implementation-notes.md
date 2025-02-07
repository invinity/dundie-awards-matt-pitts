## 1. Implement the Endpoint 
> Create the endpoint "/give-dundie-awards/{organizationId}". This endpoint should increase the number of Dundie awards for each employee in the specified organization by 1. Ensure that any related updates are also handled appropriately. 

- Implementing this endpoint as-is would have been more JSON-RPC style design than REST, since this is essentally a _verb_ as a URL. Good REST design is typically to have URLs be _nouns_ representing an actual _resource_. Mutating activities we perform on these resources would be done using the various HTTP methods (POST, PUT, DELETE). 
- For this reason, I implemented this as a _POST_ request to `/organization/{id}/employees`. This URL represents the _resource_ that is "all employees in a given organization". The _POST_ request itself will represent a `OrganizationEmployeeAction` that is to be performed on all employees of that `Organization`.
- For the specific use-case of *giving Dundie awards to all employees of an organization*, there is an action type of `GiveDundieAwardsAction`, which when _POST_ed to this REST endpoint will perform the action to give the awards.
## 2. Complete Additional Improvements 
> Address any additional improvements discussed during the call, including those you identified yourself. Please be mindful of your time—focus on changes that are manageable within the given timeframe. 
## 3. Finish Message Broker Implementation 
> Complete the implementation of the Message Broker by either introducing a library or creating a basic publish/subscribe mechanism. 
## 4. Asynchronous Activity Creation 
> Implement the creation of an Activity when awards are added to an organization. This should be done asynchronously by subscribing to notifications from the Message Broker. 
## 5. Implement Rollback Mechanism 
>Develop a mechanism to roll back the award distribution if the Activity creation fails. 