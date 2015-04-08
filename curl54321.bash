#!/bin/bash

# /home/ann/aispy/curl54321.bash

# A demo of curl-h2o-REST interaction

# This gets me a list of URL-paths AKA endpoints:
curl lh:54321/1/Metadata/endpoints.json

# This gets me a list of Frames:
curl lh:54321/3/Frames.json

# This does manual garbage collection:
# curl -X DELETE lh:54321/1/RemoveAll.json

exit
