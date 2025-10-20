## [1.0.1](https://github.com/knowledgepixels/nanopub-monitor/compare/nanopub-monitor-1.0.0...nanopub-monitor-1.0.1) (2025-10-20)

### Dependency updates

* **core-deps:** update dependency org.nanopub:nanopub to v1.83 ([380ad08](https://github.com/knowledgepixels/nanopub-monitor/commit/380ad08746ef0373707af68be7a27125c083162a))
* **core-deps:** update dependency org.slf4j:slf4j-simple to v2.0.17 ([0765483](https://github.com/knowledgepixels/nanopub-monitor/commit/076548385c923eb09d57b5d7c549ebba93c0480c))
* **deps:** add com.google.cloud.tools:jib-maven-plugin dependency ([f4a90ea](https://github.com/knowledgepixels/nanopub-monitor/commit/f4a90ead6394549e3ab54f288e3cf6429ae9ee60))
* **deps:** add semantic-release dependencies ([efc760b](https://github.com/knowledgepixels/nanopub-monitor/commit/efc760b07dda9b154ee629b9b7dfb12912404bd3))
* **deps:** update dependency org.eclipse.jetty:jetty-maven-plugin to v11.0.26 ([b5b0838](https://github.com/knowledgepixels/nanopub-monitor/commit/b5b0838c7be5ca9fec59871c96abe039db85b85b))
* **deps:** update Maven to v3.9.11 ([e6c1ce6](https://github.com/knowledgepixels/nanopub-monitor/commit/e6c1ce689a592b3cd848f807e13b3d905dd87b27))

### Bug Fixes

* **ServerList:** update API call to use QueryRef for service retrieval ([24feb24](https://github.com/knowledgepixels/nanopub-monitor/commit/24feb24d8cb6b8b3230c018dcc96ab25dd902984))

### Documentation

* add javadoc annotations ([477e3ef](https://github.com/knowledgepixels/nanopub-monitor/commit/477e3efe827976f44d2504285e79bbe304cc5bda))

### Tests

* **deps:** add dependency org.junit.jupiter:junit-jupiter v5.13.4 ([500d8bd](https://github.com/knowledgepixels/nanopub-monitor/commit/500d8bd5735e0923fd34cba0098ab433a34c44f7))
* **NanopubService:** add unit tests ([d491056](https://github.com/knowledgepixels/nanopub-monitor/commit/d4910562968dea36eb6d53a7711a2ac9d307931c))
* **ServerIpInfo:** add unit tests ([106d84e](https://github.com/knowledgepixels/nanopub-monitor/commit/106d84e3d03892244e01cb2cc149d6618fc63de3))

### Build and continuous integration

* add autorelease workflow config ([bd28ff9](https://github.com/knowledgepixels/nanopub-monitor/commit/bd28ff948242a51108de548d4f7dd59eff6aea85))
* add maven test configuration ([e21f11b](https://github.com/knowledgepixels/nanopub-monitor/commit/e21f11b4735ccefaa7b061057195e6f595d5f075))
* **deps:** update action actions/checkout to v5.0.0 ([dba11ee](https://github.com/knowledgepixels/nanopub-monitor/commit/dba11eebec3f119c8befeb3d829ade733c665285))
* remove maven-test workflow since tests are executed now in the autorelease one ([6cee16a](https://github.com/knowledgepixels/nanopub-monitor/commit/6cee16addbabd5d43126a1dbd8ae8244816dba4d))
* update autorelease configuration to use env vars for Docker credentials and image name ([cfab7a9](https://github.com/knowledgepixels/nanopub-monitor/commit/cfab7a9da43997a9015cbe6b7c6877229881f5c0))

### General maintenance

* add Jetty server configuration files for development/testing ([f1e0cf4](https://github.com/knowledgepixels/nanopub-monitor/commit/f1e0cf47109f87b00cabb1fcae4935909bae64e3))
* add Maven settings for Docker registry authentication ([9dea8e1](https://github.com/knowledgepixels/nanopub-monitor/commit/9dea8e1601b5c9d0553011399b5b79f36881748c))
* add Maven wrapper ([d7ae60b](https://github.com/knowledgepixels/nanopub-monitor/commit/d7ae60b654f61246e256bc6c62006ee9a3184785))
* add semantic-release configuration ([635c874](https://github.com/knowledgepixels/nanopub-monitor/commit/635c87410e5eee6fc518619a8a49a5fc2452690b))
* **docker-compose:** update port mapping syntax ([6f3563d](https://github.com/knowledgepixels/nanopub-monitor/commit/6f3563d62938ce144d41bc2b516e2e77ad2ebe40))
* **Dockerfile:** simplify build process and update base images ([c8b6fdd](https://github.com/knowledgepixels/nanopub-monitor/commit/c8b6fdd17af22324d436e678fb02ce231ef1524d))
* **logging:** update messages for better error handling ([76ec3ba](https://github.com/knowledgepixels/nanopub-monitor/commit/76ec3ba2df4f7e5b5cd0284017cde018bd79364d))
* update maven-compiler-plugin to use Java 21 ([ea47959](https://github.com/knowledgepixels/nanopub-monitor/commit/ea47959e0bd4704673c655666b81df09163baff6))
* update readme ([5d04b94](https://github.com/knowledgepixels/nanopub-monitor/commit/5d04b94656859efe47e6eecb56bddb10b1bc0221))
* update semantic-release configuration for Maven docker release integration ([aee4498](https://github.com/knowledgepixels/nanopub-monitor/commit/aee44989807485308f447c1ab515fce25bf535ac))
* update version to 1.0.1-SNAPSHOT to comply with semantic versioning syntax and add project description ([46797df](https://github.com/knowledgepixels/nanopub-monitor/commit/46797df16e7e0e446d523520ec45dbd5c668f1af))

### Refactoring

* replace tabs with spaces ([c8b62f5](https://github.com/knowledgepixels/nanopub-monitor/commit/c8b62f500b6d566855c880cce87286193ec40312))
* **ServerData:** replace deprecated URL constructor ([b8a9751](https://github.com/knowledgepixels/nanopub-monitor/commit/b8a97519636171a43789fc36f87c82937d3e57c3))
