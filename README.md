How it works?
--------------
```bash
    Host machine                          remote device                 
 ┌───────────────┐                     ┌──────────────────┐
 │               │                     │                  │
 │               │      ssh login      │                  │
 │               │◄───────────────────►│                  │
 │               │  interactive shell  │                  │
 │               │                     │                  │
 │               │                     │                  │
 └───────────────┘                     └──────────────────┘
```

Login to the machine via SSH and create an interactive shell. With this shell, you can run commands or scripts and tail logs. 
I don't have a specific use case or direction for this project right now. it could potentially be used for automation testing of embedded devices.?

to-do
--------------
This isn't stability and it has only been tested with a single SSH connection. and the unit tests require updating.