```
/
├── interfaces
│   ├── common
│   │   ├── interceptor
│   │   ├── security
│   │   ├── validator
│   │   └── ...
│   └── (도메인)
│       ├── Controller.java
│       └── dto
│           ├── request
│           │   └── Request.java
│           └── response
│               └── Response.java
├── application
│   ├── common
│   └── (도메인)
│       ├── Facade.java
│       ├── Command.java
│       └── Info.java
├── domain
│   ├── common
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
│   └── persistence
│       └── (도메인)
│           ├── jpa
│           │   ├── JpaRepository.java
│           │   └── QueryDslRepository.java
│           └── RepositoryImpl.java
└── config
    └── Config.java
```