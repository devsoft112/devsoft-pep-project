# Social media blog API

## Tables 
(these should be provided both as a sql script and in h2 as part of the p1 template):

### User
user_id integer primary key,
username varchar(255),
password varchar(255)

### Message
message_id integer primary key,
posted_by integer foreign key references User(user_id),
text varchar(255),
time_posted timestamp // could just use a long with epoch time

## Integration tests

Realistically, we can only test for 200 or 400 status codes provided by the REST endpoints, as well as the length of lists and presence of JSON response bodies, since we have no knowledge of how exactly the data will be modeled.

# Requirements

## Background

When building a full-stack application, we're typically concerned with both a front end, that displays information to the user and takes in input, and a backend, that manages persisted information.

This project will construct a backend for a hypothetical social media app, where we must manage User accounts as well as any messages that they submit to the application. In our hypothetical application, any user should be able to see all of the messages posted to the site, or they can see the messages posted by a particular user. In either case, we require a backend which is able to deliver the data needed to display this information as well as process requests like logins, registrations, and message creations,updates, and deletions.

## 1) API should be able to process new User registrations.

As a user, I should be able to create a new account on the endpoint POST localhost:8080/register. The body will contain a representation of a JSON User. This endpoint should return a User representation on a successful registration, and a 400 error on an unsuccessful registration. The API should persist the new user to the database so that they may log in later.

A registration should be successful when the User representation in the request contains a username that is not already in use by another user and a password exceeding 4 characters.

In the future, this action may generate a Session token to allow the user to securely use the site. We will not worry about this for now.

## 2) API should be able to process User logins.

As a user, I should be able to verify my login on the endpoint POST localhost:8080/login. The request body will contain a JSON representation of a User. The API should check if the username and password given in the record match what is contained in the database. This endpoint should return a response body containing a User JSON representation on a successful login, and a 401 error on an unsuccessful login.

In the future, this action may generate a Session token to allow the user to securely use the site. We will not worry about this for now.


## 3) API should be able to process the creation of new messages.

As a user, I should be able to submit a new post on the endpoint POST localhost:8080/messages. The request body will contain a JSON representation of a message. The API should not permit messages with text that is 0 characters long or greater than 255 characters long. The endpoint should return the new message if successful, and a 400 error if unsuccessful. The user ID who has submitted the message should be identified within the message JSON. New messages should be persisted to the database so that they may be retrieved later.


## 4) API should be able to retrieve all messages.

As a user, I should be able to submit a GET request on the endpoint GET localhost:8080/messages. The response body should contain a JSON representation of a list containing all messages ordered by their timestamp (newest messages first), which is retrieved from the database.

## 5) API should be able to delete a message identified by a message ID.

As a User, I should be able to submit a DELETE request on the endpoint DELETE localhost:8080/messages/{message_id}. If the resource was successfully deleted from the database, the response body should contain the deleted message. If unsuccessful due to a nonexistent resource, the response should be a 400 error code.

## 6) API should be able to update message text identified by a message ID.

As a user, I should be able to submit a PUT request on the endpoint PUT localhost:8080/messages/{message_id}. The request body should contain the new values to replace the message identified by message_id. If the record in the database was successfully modified, the response body should contain the newly updated message. The API should not update anything other than the message text. The API should not permit messages with text that is 0 characters long or greater than 255 characters long. The API should not change any part of a message other than its text. If unsuccessful , the response should return a 400 error code.

## 7) API should be able to retrieve all messages written by a particular user.

As a user, I should be able to submit a GET request on the endpoint GET localhost:8080/users/{user_id}/messages. The body should contain a JSON representation of a list containing all messages posted by a particular user ordered by their timestamp (newest messages first), which are retrieved from the database.

## 8) API should be able to retrieve how many messages were written by users.

As a user, I should be able to submit a GET request on the endpoint GET localhost:8080/users/messageCounts. The body should contain data that identifies key-value pairs of users (identified either by user ID or username) and the total number of posts that user has made. It's recommended to use the count() aggregate function and the group by command.