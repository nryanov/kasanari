# Kasanari

Kasanari (重なり) is an open-source catalog for Apache Iceberg, Apache Paimon and Lance. 
The main purpose is to provide unified service which implements REST APIs enabling seamless multi-engine interoperability across a wide range of platforms.

Kasanari can be used not only as a catalog implementation, but also as a proxy for already existing catalog instance. It may be useful for cases such as:
- Migration from old catalog to the new one. Migrating to the `rest` allows to switch catalog implementation in the future if needed without rewriting code
- Some engines may not support concrete catalog implementation. Proxying requests via rest specification allows to use existing catalog even in this case

## Implemented catalogs

- Apache Iceberg: `1.10.1`
- Apache Paimon: `1.4.1`
- Lance: `0.7.2`

Each catalog may operate in different mode: 
- `INTERNAL`: Kasanari owns the catalog metadata lifecycle
- `PROXY`: Kasanari forwards operations to upstream catalog implementations while keeping a unified API gateway and security layer.

It is also possible to set multiple INTERNAL/PROXY catalog of the same type but for different purposes.

## Where to go next

- Start locally: `quickstart.md`
- Review catalog support boundaries: `catalogs/iceberg.md`, `catalogs/paimon.md`, `catalogs/lance.md`
- Explore client integrations: `integrations/index.md`
- Configure auth/authz and RBAC: `security/index.md`
- Configure metrics and tracing: `observability/index.md`
- Browse runnable examples: `examples/index.md`
