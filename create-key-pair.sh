#!/bin/sh

ENVIRONMENT=${1}
REGION=eu-west-2
KID="${ENVIRONMENT}-$(uuidgen)"

function createKeyPair {
  openssl genrsa -out $KID.pem 4096
  openssl rsa -in $KID.pem -pubout -outform PEM -out $KID.pem.pub
  openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in $KID.pem -out pkcs8.key

  MODULUS=$(
      openssl rsa -pubin -in $KID.pem.pub -noout -modulus `# Print modulus of public key` \
      | cut -d '=' -f2                                    `# Extract modulus value from output` \
      | xxd -r -p                                         `# Convert from string to bytes` \
      | openssl base64 -A                                 `# Base64 encode without wrapping lines` \
      | sed 's|+|-|g; s|/|_|g; s|=||g'                    `# URL encode as JWK standard requires`
  )

  echo '{
    "keys": [
      {
        "kty": "RSA",
        "n": "'"$MODULUS"'",
        "e": "AQAB",
        "alg": "RS512",
        "kid": "'"$KID"'",
        "use": "sig"
      }
    ]
  }' > $KID.json
}

function addPrivateKeyToParameterStore {
  PRIVATE_KEY=$(cat pkcs8.key)
  PUBLIC_KEY=$(cat $KID.json)

  aws ssm put-parameter \
        --name /prs/${ENVIRONMENT}/user-input/pds-fhir-private-key \
        --value "$PRIVATE_KEY" \
        --type SecureString \
        --overwrite

  aws ssm put-parameter \
       --name /prs/${ENVIRONMENT}/user-input/pds-fhir-public-key \
       --value "$PUBLIC_KEY" \
       --type String \
       --overwrite
}

if [[ -z "${ENVIRONMENT}" ]]; then
    echo "Must set ENVIRONMENT"
    exit 1
fi

echo "creating key pair..."
createKeyPair

echo "saving public and private key to parameter store..."
addPrivateKeyToParameterStore

echo "cleaning up project directory..."
rm $KID.pem $KID.pem.pub $KID.json

echo "finished"