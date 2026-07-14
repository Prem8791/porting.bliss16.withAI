# P0 Implementation Dependency Graph

This graph is normative for milestone readiness. Dotted relationships represent
parallel work that must join before integration.

```mermaid
flowchart TD
  P000[P0-00 Reference lock / G0] --> P001[P0-01 Repository + test skeleton]
  P001 --> P002[P0-02 Contract runtime + vectors / G1]
  P002 --> P003[P0-03 Framework projection + interfaces / G2]
  P003 --> P004[P0-04 Authority identity/lifecycle]
  P004 --> P005[P0-05 Registry]
  P005 --> P006[P0-06 Policy + authorization]
  P002 --> P007[P0-07 Audit]
  P006 --> P008[P0-08 SystemUI + Settings]
  P007 --> P008
  P005 --> P009[P0-09 Broker]
  P006 --> P009
  P007 --> P009
  P003 --> P010[P0-10 Provider SDK + no-op]
  P006 --> P010
  P009 --> P010
  P005 --> P011[P0-11 Observation/Event]
  P006 --> P011
  P007 --> P011
  P010 --> P011
  P005 --> P012[P0-12 Extension quarantine]
  P004 --> P012
  P004 --> P013[P0-13 Security packaging / G3]
  P007 --> P013
  P009 --> P013
  P011 --> P013
  P012 --> P013
  P008 --> P014[P0-14 Product/APEX integration]
  P010 --> P014
  P013 --> P014
  P014 --> P015[P0-15 Shadow hardening / G5 G6 G7]
```

## Parallel lanes

- After G1: framework interface planning, Audit and SDK/Extension test-vector
  scaffolding may proceed in parallel.
- After P0-04: Registry and Audit may proceed independently.
- After Registry/Policy interface freeze: Broker, SystemUI/Settings, SDK,
  Observation and Extension teams may work against reviewed fakes.
- Security policy may be designed early but no final allow is added before the
  real endpoint/process/storage graph stabilizes.
- Packaging metadata may be prepared early but no product graph is installed
  before component G4 and G3.

Critical path:

`P0-00 -> P0-01 -> P0-02/G1 -> P0-03/G2 -> P0-04 -> P0-05 -> P0-06 ->
P0-09 -> P0-13/G3 -> P0-14 -> P0-15/G7`

