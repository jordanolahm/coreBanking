# CoreBanking Postman Collection
-> Use this tool to organize the sequence of REST API calls in project. 
## Como usar
1. Open postman.
2. Click in **Import** set this file to imports into postman structure `CoreBanking-Collection.json`.
3. Configure the registration URL in `baseUrl`:
    - `http://localhost:8080` to local environment.
    - Port common 8080, configure in project environment inside to resources.
4. All routes will be ready to operate. you will need understand the sequence of body data and the required request values. 
5. Principal endpoints:
    - POST `/api/accounts` → Create account
    - GET `/api/accounts/{accountId}` → Search account
    - GET `/api/accounts/balance` → Return balance
    - POST `/api/accounts/overdraft` → Set overdraft
    - POST `/api/accounts/reset` → Reset Data
    - POST `/api/transactions` → Create transaction
    - POST `/api/transactions/event` → Handle event to operate
    - GET `/api/transactions/{transactionId}` → Search transaction
    - GET `/api/transactions/today` → List transactions of day
    - GET `/api/transactions/range` → List transactions in range date
    - GET `/api/transactions/type/{operationTypeId}` → List transactions by type