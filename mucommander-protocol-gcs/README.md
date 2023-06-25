# Google cloud storage protocol

This protocol uses Google cloud libraries to access Cloud storage service.

## About protocol
The protocol uses non-standard url schema `gcs://` so we can utilize bucket-level operations in a project. 
Google cloud storage syntax can be found [here](https://cloud.google.com/storage/docs/gsutil#syntax).

For example the object with gsutil url `gs://BUCKET_NAME/OBJECT_NAME` has url `gcs://PROJECT_NAME/BUCKET_NAME/OBJECT_NAME`
using this protocol. Thanks to this small change, we can connect to the specific project and list all the buckets as the root folder.

## Features
- Bucket-level support in the Google Cloud Project
- The implementation supports standard bucket and blob (i.e. file/folder) CRUD operations
- [Service account impersonation](https://cloud.google.com/iam/docs/service-account-overview#impersonation)
- Storing connection properties as plaintext json - there are no secrets
  - Thanks to that, you can open a previous connection from the Bookmarks panel
- Google Cloud application default credentials support
  - To use the defaults gcloud utils has to be installed, see [here](https://cloud.google.com/sdk/docs/install)
  - The application default credentials need to be initialized, see [here](https://cloud.google.com/docs/authentication/application-default-credentials#personal)