# Architecture Index

This index is not the full contract. Do not implement from this file alone; follow the artifact paths below.

## Implementation Guide

### Global artifacts

- `unit_graph.yaml`: all workers; filter by assigned unit for entrypoints, source anchors, and dependencies.
- `wire_contracts.yaml`: workers touching APIs; filter by `unit` for implemented request/response/error contracts.
- `shared_modules.yaml`: all workers; currently empty because no source file is shared by two units.
- `cross_unit_state.yaml`: workers handling authentication or state; currently empty because no paired implicit state flow was found.
- `project-structure.md`, `tech-stack.md`, `data-model.md`: design context and evidence; these describe the existing code, while documentation-only features are explicitly marked.

For every unit, completion evidence must report artifact paths read, implemented `must_preserve` and `must_appear_in_target` items, unresolved/deferred contracts, and build/test/runtime evidence.

### Unit: api-gateway-routing

- Trigger: reactive `/api/v1/**` gateway routes.
- Read `units/api-gateway-routing/behavior.yaml`, `bindings.yaml`, and `unit_decomposition.yaml`.
- Filter `wire_contracts.yaml` rows with `unit: api-gateway-routing` and read gateway route configuration directly.

### Unit: identity-auth-profile

- Trigger: authentication and current-user profile routes.
- Read `units/identity-auth-profile/behavior.yaml`, `bindings.yaml`, and `unit_decomposition.yaml`.
- Filter `wire_contracts.yaml` where `unit: identity-auth-profile`; read Identity migrations and runtime configuration.

### Unit: notification-management

- Trigger: notification list, create, unread-count, read/unread, seed, and delete routes.
- Read `units/notification-management/behavior.yaml`, `bindings.yaml`, and `unit_decomposition.yaml`.
- Filter `wire_contracts.yaml` where `unit: notification-management`; verify recipient ownership and the documented/schema identifier mismatch.

### Units: recruitment-service-shell, candidate-ai-service-shell, subscription-service-shell, interview-service-shell, analytics-service-shell

- Trigger: documented API family, but source currently contains no controller or domain implementation.
- Read each unit's three per-unit artifacts and `unit_graph.yaml`; treat documented endpoints as planned evidence, not implemented contracts.
- Before implementation, reconcile the unit's docs, gateway route, Docker/Compose configuration, and actual source entrypoints.

## Scope note

The documentation describes the desired seven-service architecture and many future contracts. The artifacts distinguish those claims from source-backed behavior so that design work does not silently treat planned endpoints as available.
