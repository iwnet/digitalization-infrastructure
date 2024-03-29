## GENERATE SELF SIGNED CERTIFICATE FOR KAFKA SERVER AND CLIENTS
### commands (fill in all prompts the same password)

```
keytool -keystore server.keystore.jks -alias server_host -validity 365 -genkey -keyalg RSA
keytool -keystore client.keystore.jks -alias client_host -validity 365 -genkey -keyalg RSA

openssl req -new -x509 -keyout ca-key -out ca-cert -days 365

keytool -keystore server.keystore.jks -alias server_host -certreq -file cert-file.csr
keytool -keystore client.keystore.jks -alias client_host -certreq -file cert-file-cl.csr

keytool -keystore server.keystore.jks -alias CARoot -import -file ca-cert
keytool -keystore client.keystore.jks -alias CARoot -import -file ca-cert

openssl x509 -req -CA ca-cert -CAkey ca-key -in cert-file.csr -out server-signed-cert \
  -days 10000 -CAcreateserial
openssl x509 -req -CA ca-cert -CAkey ca-key -in cert-file-cl.csr -out client-signed-cert \
  -days 10000 -CAcreateserial

keytool -keystore server.keystore.jks -alias server_host -import -file server-signed-cert
keytool -keystore client.keystore.jks -alias client_host -import -file client-signed-cert 

keytool -keystore server.truststore.jks -alias CARoot -import -file ca-cert 
keytool -keystore client.truststore.jks -alias CARoot -import -file ca-cert

cp client.keystore.jks ../../bda/ssl/
cp client.truststore.jks ../../bda/ssl/

keytool -exportcert -alias client_host -keystore client.keystore.jks -rfc -file certificate.pem
keytool -v -importkeystore -srckeystore client.keystore.jks -srcalias client_host -destkeystore cert_and_key.p12 -deststoretype PKCS12
openssl pkcs12 -in cert_and_key.p12 -nodes -nocerts -out key.pem
keytool -exportcert -alias client_host -keystore client.keystore.jks -rfc -file CARoot.pem 
cp certificate.pem ../../spark/ssl/
cp key.pem ../../spark/ssl/
cp CARoot.pem ../../spark/ssl/

```
