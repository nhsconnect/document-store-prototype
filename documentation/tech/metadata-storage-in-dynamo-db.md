### Document metadata storage

#### Overview of DynamoDB

High level overview: https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/HowItWorks.CoreComponents.html

Concepts:
* Table - collection of data
* Item - unique entry, a collection of attributes; a collection of items make a table
* Attribute - fundamental data element; has a type (eg. number, string, list); list of attributes can be different for items in the same table
* Primary key - either a partition key or partition key + sort key
* Secondary Index - enabling fast way of querying data for values of a certain field
* GetItem - retrieve item from table knowing whole primary key
* Query - retrieve item(s) from table or index knowing partition key and (potentially) filtering on sort key
* Scan - retrieve item(s) from table or index by going through all entries and applying filters on any attributes; less efficient for finding items than query

AWS provides a high level way of interacting with DynamoDB for CRUD operations - https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/DynamoDBMapper.html 

#### Design metadata entity - first draft

Table: Metadata

Primary Key: UUID (generated in the application code that's unique per document)

Attributes: 
* NHS Number
* Title
* Description 
* Document clinical type
* S3 location

Global secondary indexes:
* NHS Number 
* Document clinical type
* S3 location

#### Maintenance

Use terraform to create and update tables. 
   