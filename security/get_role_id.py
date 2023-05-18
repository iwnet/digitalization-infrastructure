#! /usr/bin/env python3

import json
import sys

# some JSON:
x = sys.argv[1].strip()
requested_role = sys.argv[2]

# parse x:
y = json.loads(x)

for jObj in y:
  if jObj["name"] == requested_role:
    # the result is a Python dictionary:
    print(jObj["id"])
    break

