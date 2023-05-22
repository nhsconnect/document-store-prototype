# Access Request Fulfilment (ARF) Service Virus Scanner

We have added a virus scanner to the application in order to passively scan all files that are uploaded to the document store S3 bucket.

This usually just runs as a separate pipeline, and does not require much maintenance once it's running unless changes are made.



## Table of Contents

1. [Running Locally](#running-locally)
2. [Usage](#usage)
3. [Maintenance](#maintenance)

## Running locally

The virus scanner should be built and added to your localstack automatically

It is created by using a CloudFormation stack template to provision the required resources.- 

## Usage

Please be aware that the Virus Scanner does slow down the uploading process. Once you upload a document to a patient, there may be some minutes in between the upload completing and the file becoming available in the frontend.

The scanner is set to scan a single bucket and move any files found to contain viruses into a Quarantine Bucket that is auto-generated by the stack. This happens automatically without you needing to do anything. 

If you need an example, please use the [Eicar File](https://www.eicar.org/download-anti-malware-testfile/), but please remember that downloading this file will likely cause a false positive on your computer's virus scanner. 

## Maintenance

There is a dedicated [Confluence document](https://gpitbjss.atlassian.net/wiki/spaces/TW/pages/12406227067/Virus+Scanner+maintenance) discussing how to maintain the Virus Scanner stack in a live environment. Please refer to this document for any issues. 