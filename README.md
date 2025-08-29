# sms_validator
A small implementation of sms validator with in memory database for users and saved sms. Phishing detection uses gcloud webrisk api.

## building and running the service
```shell
docker build -t aTagForImage .
docker image push aTagForImage
docker run \
  -p 8080:8080  \
  -e WEB_RISK_API_KEY=yourKey \
  aTagForImage
```
App's port will be available at `localhost:8080`

## operations
All operations are triggered by http requests:

`POST /permissions/grant/:userId` - saves user with granted permission to save sms info  
`POST /permissions/revoke/:userId` - saves user with revoked permission to save sms info  
`POST /sms` - tries to save sms after validation for phishing
