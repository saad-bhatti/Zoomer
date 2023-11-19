import requests

userUrl = 'http://localhost:8001'

locationUrl = 'http://localhost:8000'

tripUrl = 'http://localhost:8002'

#Configurations
userObj = {
  "name": "TestName354",
  "email": "test@gmail.com",
  "password": "ILoveCS"
}

#Drivers
Drivers = [
  {
  "name": "Ritvik",
  "email": "ritvik@gmail.com",
  "password": "Ritvik"
},
{
  "name": "Kyrel",
  "email": "Kyrel@gmail.com",
  "password": "Kyrel"
},
{
  "name": "Ethan",
  "email": "Ethan@gmail.com",
  "password": "Ethan"
},
{
  "name": "Daniil",
  "email": "Daniil@gmail.com",
  "password": "Daniil"
},
{
  "name": "Nivy",
  "email": "Nivy@gmail.com",
  "password": "Nivy"
},
{
  "name": "Chris",
  "email": "Chris@gmail.com",
  "password": "Chris"
},
{
  "name": "Lance",
  "email": "Lance@gmail.com",
  "password": "Lance"
},
{
  "name": "Manav",
  "email": "Manav@gmail.com",
  "password": "Manav"
}
]


Roads = [
  {
    "roadName": "U of T Underpass",
    "hasTraffic": True
  },
  {
    "roadName": "Liut Lights",
    "hasTraffic": False
  },
  {
    "roadName": "MN Drive",
    "hasTraffic": True
  },
  {
    "roadName": "Bergen Blvd",
    "hasTraffic": False
  },
  {
    "roadName": "Zingaro Zone",
    "hasTraffic": True
  },
  {
    "roadName": "Sushant Summit",
    "hasTraffic": True
  },
  {
    "roadName": "CCIT Corner",
    "hasTraffic": False
  },
  {
    "roadName": "Deerfield Dash",
    "hasTraffic": False
  },
  {
    "roadName": "Ilir Isle",
    "hasTraffic": False
  },
  {
    "roadName": "IB Island",
    "hasTraffic": False
  },
  {
    "roadName": "Sonya Street",
    "hasTraffic": True
  },
  {
    "roadName": "Petersen Park",
    "hasTraffic": False
  },
  {
    "roadName": "Kaneff St West",
    "hasTraffic": True
  },
  {
    "roadName": "Sauga Skyway",
    "hasTraffic": False
  },
  {
    "roadName": "Lisa Lane",
    "hasTraffic": False
  },
  {
    "roadName": "Davis Dungeon",
    "hasTraffic": False
  },
  {
    "roadName": "Michael Meadows",
    "hasTraffic": True
  }
]

RoadConnections = {
  "U of T Underpass": [("Liut Lights", 4), ("Bergen Blvd", 10), ("Deerfield Dash", 14), ("Sonya Street", 20), ("Sauga Skyway", 22), ("Kaneff St West", 24), ("Michael Meadows", 30)],
  "Liut Lights": [("MN Drive", 4)],
  "Bergen Blvd": [("Zingaro Zone", 4)],
  "Zingaro Zone": [("Sushant Summit", 4)],
  "Sushant Summit": [("CCIT Corner", 4)],
  "Deerfield Dash": [("Ilir Isle", 6)],
  "Sonya Street": [("IB Island", 4), ("Petersen Park", 8)],
  "Sauga Skyway": [("Lisa Lane", 8)],
  "Kaneff St West": [("Lisa Lane", 8)],
  "Michael Meadows": [("Davis Dungeon", 10)]
}

def createUser(userObj, isDriver):
  req = requests.post(userUrl + '/user/register', json=userObj)
  uid = None
  if (req.status_code == 200):
    uid = req.json()['uid']
    req = requests.put(locationUrl + '/location/user', json={"uid": uid, "is_driver": isDriver})
    print(req)
    if (req.status_code == 200):
      req = requests.patch(locationUrl + f'/location/{uid}',json={"latitude": 0, "longitude": 0, "street": "Liut Lights"})
      print(req)
  return uid

print("Creating User...")
# Create user
createUser(userObj, False)

print("Creating Drivers...")
for driver in Drivers:
  uid = createUser(driver, True)
  if uid != None:
    req = requests.patch(userUrl + f'/user/{uid}', json={"isDriver": True})
    print(req)

print("Creating Roads...")
# Create Roads
for road in Roads:
  req = requests.put(locationUrl + '/location/road', json=road)
  print(req)

print("Creating Road Connections...")
# Create Road Conncetions
for roadOne, roadTups in RoadConnections.items():
  for roadTwo, time in roadTups:
    req = requests.post(locationUrl + '/location/hasRoute',json = {
      "roadName1": roadOne,
      "roadName2": roadTwo,
      "time": time,
      "hasTraffic": False
    })
    print(req)

    # These are all two way roads
    req = requests.post(locationUrl + '/location/hasRoute',json = {
      "roadName1": roadTwo,
      "roadName2": roadOne,
      "time": time,
      "hasTraffic": False
    })
    print(req)

print("Done creating data.")

