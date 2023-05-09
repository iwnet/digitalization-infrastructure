#! /usr/bin/env python3

import json
import sys

# some JSON:
x = sys.argv[1].strip()

# parse x:
y = json.loads(x)

# the result is a Python dictionary:
print(y["access_token"])

