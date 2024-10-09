```
/
├── interfaces
│   └── (도메인)
│       ├── Controller.java
│       └── dto
│           ├── request
│           │   └── Request.java
│           └── response
│               └── Response.java
├── application
│   └── (도메인)
│       ├── Facade.java
│       ├── Command.java
│       └── Info.java
├── domain
│   └── (도메인)
│       ├── Entity.java
│       ├── Service.java
│       ├── enumeration
│       │   └── Enumeration.java
│       ├── exception
│       │   └── Exception.java
│       └── repository
│           └── Repository.java
├── infrastructure
│   ├── jwt
│   │   └── JwtUtils.java
│   └── persistence
│       └── (도메인)
│           ├── jpa
│           │   ├── JpaRepository.java
│           │   └── QueryDslRepository.java
│           └── RepositoryImpl.java
└── global
    ├── config
    │   └── Config.java
    └── web
        ├── interceptor
        ├── security
        ├── validator
        └── ...
```