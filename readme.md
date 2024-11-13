# Core Banking API

This is a simple RESTFull API for managing account balances using Spring Boot. It provides functionality to reset the database, view account balances, and handle deposit, withdrawal, and transfer events.

### Key features
- State Reset: Restores the system to its initial state, removing all account and transaction data.

- Balance Inquiry: Allows users to check the balance of a specific account via an account ID parameter. If the account does not exist, exist handle error system for appropriate error code.

- Event Operations: The system uses a single endpoint to manage financial events of three actions (types):
  - Deposit: Adds funds to an account. If the account does not exist, it is automatically created with the specified balance.
  - Withdrawal: Deducts funds from an existing account. If the account lacks sufficient balance or does not exist, the operation fails.
  - Transfer: Moves funds from an origin account to a destination account. Both balances are updated simultaneously, or the operation fails if any of the accounts do not exist or have insufficient funds.

## Server Port
[http://localhost:8080/api](http://localhost:8080/api)

## Endpoints

### 1. Reset State
- **Method:** `POST`
- **Endpoint:** `/reset`
- **Description:** Resets all accounts and balances, bringing the application back to its initial state.
- **Request Example:**
  ```bash
  curl -X POST "http://localhost:8080/api/reset"
  
### 2. Get Balance
- **Method:** `GET`
- **Endpoint:** `/balance`
- **Description:** Retrieves the balance of a specified account.
- **Parameters:** account_id (String): The identifyer of the account whose balance is to be retrieved.
- **Request Example:**
  ```bash
  curl -X GET "http://localhost:8080/api/balance?account_id=1234"

### 3. Handle Event
- **Method:** `POST`
- **Endpoint:** `/event`
- **Description:** Handles deposit, withdrawal, or transfer events for accounts.
- **Parameters:** 
    #### type (String) specifies the type of event.
    #### destination (String) the id of account.
    #### origin (String) the identifyer o account from which the withdraw or transfer funds.
    #### amounts (BigDecimal) the amout to be transacted - transaction amount.

- **Deposit to new Account:**
  ```bash
  curl -X POST "http://localhost:8080/api/event" -H "Content-Type: application/json" -d '{"type":"deposit", "destination":"100", "amount":10}'

- **Deposit to Existing Account:**
  ```bash
  curl -X POST "http://localhost:8080/api/event" -H "Content-Type: application/json" -d '{"type":"deposit", "destination":"100", "amount":10}'

- **Withdraw from Non-Existing Account:**
  ```bash
  curl -X POST "http://localhost:8080/api/event" -H "Content-Type: application/json" -d '{"type":"withdraw", "origin":"200", "amount":10}'

- **Withdraw from Existing Account:**
  ```bash
  curl -X POST "http://localhost:8080/api/event" -H "Content-Type: application/json" -d '{"type":"withdraw", "origin":"100", "amount":5}'

- **Withdraw from Existing Account:**
  ```bash
  curl -X POST "http://localhost:8080/api/event" -H "Content-Type: application/json" -d '{"type":"withdraw", "origin":"100", "amount":5}'

- **Transfer from Existing Account:**
  ```bash
  curl -X POST "http://localhost:8080/api/event" -H "Content-Type: application/json" -d '{"type":"transfer", "origin":"100", "amount":15, "destination":"300"}'

- **Transfer from Non-Existing Account:**
  ```bash
  curl -X POST "http://localhost:8080/api/event" -H "Content-Type: application/json" -d '{"type":"transfer", "origin":"200", "amount":15, "destination":"300"}'


### Notes:
- This structure was built with [Spring Initializr](https://start.spring.io/).
- This API was built with Spring Initializr.
- Java version: 17
- Project management: Maven