aws amplify create-deployment --app-id $AWS_AMPLIFY_APP_ID --branch-name dev > deployment.output
jobId=$(jq -r .jobId deployment.output)
zipUploadUrl=$(jq -r .zipUploadUrl deployment.output)
echo $jobId
echo $zipUploadUrl
curl -XPUT --data-binary "@ui/build/hello.zip" ${zipUploadUrl}
aws amplify start-deployment --app-id $AWS_AMPLIFY_APP_ID --branch-name dev --job-id ${jobId}
rm -f deployment.output