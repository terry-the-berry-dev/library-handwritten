There two open endpoints:
`POST /auth/signup`
`POST /auth/login`

Both accept `Content-Type: application/json` in a form of
```json
{
    "username": "admin",
    "password": "pass"
}
```
 
Signup will return a user json representation upon successful signup.

The login end point will return a logged in user with `Authorization` header that contains a jwt token. 

All other endpoints can be accessed using this jwt token in a form of header like this:
`Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIiwiaWF0IjoxNzMxMzYyNDI1LCJleHAiOjE3MzEzNjYwMjV9._HBMzZpzUoiyMm-i688C-vs2plEhH0HiDy8GfaALl1s`
