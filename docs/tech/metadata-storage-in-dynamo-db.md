# Metadata Storage In DynamoDB

## DynamoDB Overview

[High-level overview](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/HowItWorks.CoreComponents.html)

Concepts:

* Table - Collection of data.
* Item - Unique entry; a collection of attributes; a collection of items make a table.
* Attribute - Fundamental data element; has a type (eg. number, string, list); list of attributes can be different for
  items in the same table.
* Primary key - Either a partition key or partition key + sort key.
* Secondary Index - Enabling fast way of querying data for values of a certain field.
* GetItem - Retrieve item from table knowing whole primary key.
* Query - Retrieve item(s) from table or index knowing partition key and (potentially) filtering on sort key.
* Scan - Retrieve item(s) from table or index by going through all entries and applying filters on any attributes; less
  efficient for finding items than query.

AWS provides a high level way of interacting with DynamoDB for CRUD
operations
called [DynamoDBMapper](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/DynamoDBMapper.html).

## Design Metadata Entity

* Table: Metadata 
* Primary Key: UUID (generated in the app code that's unique per doc)
* Attributes:
  * NHS Number
  * Title
  * Filename
  * Doc clinical type
  * S3 location 
* Global secondary indexes:
  * NHS Number
  * Doc clinical type
  * S3 location

_Note: This is the first draft and may change in the future._

## Maintenance

Use Terraform to create and update tables. 
   