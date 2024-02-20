# order-management

1. install fake-gcs-server https://github.com/fsouza/fake-gcs-server

# pull fake-gcs-server
docker pull fsouza/fake-gcs-server

# build container
docker run -d --name fake-gcs-server -p 4443:4443 fsouza/fake-gcs-server -scheme http

2. build service


