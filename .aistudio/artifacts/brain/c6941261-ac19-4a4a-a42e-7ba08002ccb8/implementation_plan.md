# EngineerKit — Phase 2: Scientific Calculation Engine & 10 Core Calculators

Phase 2 introduces a dedicated, high-precision engineering calculation engine and 10 fully functional native calculators into EngineerKit. The implementation separates mathematical logic from UI components, features an extensible dimensional unit conversion engine, and provides interactive, responsive Jetpack Compose calculator interfaces with step-by-step mathematical breakdowns.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The architectural design incorporates the three choices confirmed during Phase 2 clarification:

- **Confirmed Decision 1: Scientific Calculator Keypad**: The scientific calculator will feature an integrated tactile keypad with dedicated quick-tabs for Trigonometry (`sin`, `cos`, `tan`, `asin`, `acos`, `atan`), Scientific/Logarithmic (`ln`, `log10`, `e`, `π`, `^`, `√`), and Standard Operations, avoiding cumbersome nested menus while keeping the expression display prominent.
- **Confirmed Decision 2: Step-by-Step Mathematical Derivations**: Result cards will include an **expandable accordion view** that displays the formula with substituted user values, intermediate steps, dimensional sanity checks, and engineering assumptions.
- **Confirmed Decision 3: Smart Unit Conversion on Input Switch**: When the user switches a parameter's unit dropdown (e.g., changing pressure from `bar` to `MPa` or length from `mm` to `m`), the engine automatically converts the existing input numeric value to represent the identical physical quantity.

---

## 1. Overview & Core Concept

- **What It Delivers**: 10 production-grade calculators covering applied mathematics, physics, electrical engineering, mechanics, thermodynamics, and industrial operations:
  1. **Calculatrice scientifique**: Safe recursive-descent expression parser with trigonometric, hyperbolic, exponential, logarithmic, and power functions (DEG/RAD modes, domain checking).
  2. **Convertisseur d'unités**: 12 physical dimensions (Length, Area, Volume, Mass, Force, Pressure, Torque, Power, Energy, Temperature, Time, Angular velocity) with bidirectional instant conversion and affine temperature scaling.
  3. **Équations du second degré**: Real and complex root solver with discriminant derivation and linear degeneracy handling.
  4. **Statistiques descriptives**: Mean, median, sum, variance, and standard deviation for datasets with flexible delimiters (commas, semicolons, whitespace).
  5. **Contrainte axiale (RDM)**: Tension/compression normal stress $\sigma = F / A$, MPa/$N\cdot mm^{-2}$, and yield strength ratio with non-certified design disclaimers.
  6. **Masse et volume géométrique**: Prisms, cylinders, hollow cylinders, spheres, and plates with comprehensive material density presets (Steel, Aluminum, Concrete, Copper, Water, etc.) and manual density input.
  7. **Poutre sur deux appuis (Flexion & Flèche)**: Point load and uniformly distributed load with moment diagrams, max deflection, and stress calculation.
  8. **Loi d'Ohm & Puissance**: Multivariable solver for $V, I, R, P$ allowing any 2 known inputs to solve the remaining variables.
  9. **Puissance triphasée équilibrée**: Active $P$, reactive $Q$, and apparent $S$ powers with power factor $\cos\varphi$ validation.
  10. **TRS / OEE Industriel**: Availability, Performance, Quality, and OEE percentages with warning states for abnormal performance metrics.
- **Target Audience**: Engineering students, design technicians, and practicing engineers needing rapid, certified, offline-verified calculations on mobile.

---

## 2. User Experience & Visual Design

### Screen Layouts & Component Flow

```
┌────────────────────────────────────────────────────────┐
│  TopAppBar: [← Back] Calculator Name    [★ Favorite]   │
├────────────────────────────────────────────────────────┤
│  Category Chip + Discipline Indicator                  │
│  Technical Description & Scope                         │
├────────────────────────────────────────────────────────┤
│  Input Parameters Section:                             │
│  ┌───────────────────────┬───────────────────────────┐ │
│  │ Numeric Field [ 150 ] │ Unit Dropdown [ MPa ▼ ]   │ │
│  └───────────────────────┴───────────────────────────┘ │
│  (Validation message if out of physical bounds)        │
├────────────────────────────────────────────────────────┤
│  Action Row:  [ Réinitialiser ]    [ Calculer ]        │
├────────────────────────────────────────────────────────┤
│  Result Card (High-Contrast Elevation):                │
│  • Primary Calculated Output:  24.50 MPa               │
│  • Secondary Conversions:      24.50 N/mm²             │
│  • Status / Safety Margin:     s = 2.15 (Sûr)          │
│  • [Copy Result] Action                                │
├────────────────────────────────────────────────────────┤
│  Expandable Accordion: "Détails & Étapes de Calcul"   │
│  ▼ Formule canonique : σ = F / A                       │
│    Substitution     : σ = 150 000 N / 6 122.45 mm²     │
│    Hypothèses       : Matériau homogène, RDM linéaire  │
│    Avertissements   : Non certifié pour fatigue/choc   │
└────────────────────────────────────────────────────────┘
```

- **Interactive Scientific Keypad**: For Calculator 1, an optimized 5-row responsive grid with quick-access tabs (`Base`, `Trig`, `Func`) and instant cursor evaluation.
- **Unit Converter Layout**: Visual two-column source/destination cards with a central "Swap" button ($\rightleftarrows$) and instant reactive recalculation.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Pure Kotlin Domain Layer without Android Dependencies**:
  - *Chosen Approach*: Put all calculation algorithms, parsers, and conversions in pure Kotlin packages (`com.example.domain.*`).
  - *Why*: Allows ultra-fast unit testing with standard JUnit on JVM without Android runtime overhead, ensuring numerical precision and robustness.
- **Decision 2: Safe Expression Parser (No Eval)**:
  - *Chosen Approach*: Recursive-descent AST parser for mathematical expressions supporting tokenization, operator precedence, parentheses, and math functions.
  - *Why*: Prevents security vulnerabilities, executes in microseconds, and provides precise syntax error positioning (e.g., missing closing parenthesis or division by zero).
- **Decision 3: Affine Temperature Conversions**:
  - *Chosen Approach*: Temperature conversions implement $T_{target} = a \cdot T_{source} + b$ rather than multiplicative factors, guaranteeing exact conversions between Celsius, Kelvin, and Fahrenheit.
- **Decision 4: Seamless Navigation Integration**:
  - *Chosen Approach*: If a tool has `isAvailable = true`, tapping its card navigates directly to `CalculatorScreen(calculatorId)`. If still planned, it continues to open `ToolSpecBottomSheet`.

---

## 4. Technical Architecture & Component Structure

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           UI Layer (Compose)                            │
│  • CalculatorScreen (Generic scaffold for engineering calculators)      │
│  • ScientificCalculatorScreen (Dedicated formula display & keypad)     │
│  • UnitConverterScreen (Bidirectional dimension converter)              │
│  • CalculatorViewModel (StateFlow, input buffering, unit sync)          │
└────────────────────────────────────┬────────────────────────────────────┘
                                     │
                                     ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                      Domain Layer (Pure Kotlin)                         │
│  ┌───────────────────────┐ ┌──────────────────────────────────────────┐ │
│  │   UnitEngine          │ │   ExpressionEvaluator (Safe Parser)      │ │
│  │   (12 Dimensions,     │ │   (Lexer, Tokenizer, Recursive Descent,  │ │
│  │    SI Normalization)  │ │    Trig/Log/Powers, Deg/Rad Modes)       │ │
│  └───────────────────────┘ └──────────────────────────────────────────┘ │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │   Calculator Registry (10 Implementations)                       │   │
│  │   • ScientificCalc       • UnitConverter      • QuadraticSolver  │   │
│  │   • StatisticsEngine     • AxialStressCalc    • MassVolumeCalc   │   │
│  │   • BeamBendingCalc      • OhmsLawCalc        • ThreePhasePower  │   │
│  │   • OeeTrsCalc                                                   │   │
│  └──────────────────────────────────────────────────────────────────┘   │
└────────────────────────────────────┬────────────────────────────────────┘
                                     │
                                     ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                       Data Layer & Catalog Sync                         │
│  • ToolCatalog: update the 10 tool records to isAvailable = true        │
│  • PreferencesRepository: sync favorite states and recent tools         │
└─────────────────────────────────────────────────────────────────────────┘
```

### Data Models & Contracts
- `CalculationResult<T>`: `Success(data, steps, assumptions, warnings)` vs `Error(message, invalidFieldId)`
- `CalculationStep`: `title`, `formula`, `substitution`, `intermediateValue`
- `UnitDimension`: `LENGTH`, `AREA`, `VOLUME`, `MASS`, `FORCE`, `PRESSURE`, `TORQUE`, `POWER`, `ENERGY`, `TEMPERATURE`, `TIME`, `ANGULAR_VELOCITY`
- `EngineeringUnit`: `symbol`, `name`, `toBaseFactor`, `offset` (for affine conversions)

---

## 5. Implementation Sequence

1. **Step 1: Domain Units & Core Models**:
   - Create `com.example.domain.units` (`UnitDimension`, `EngineeringUnit`, `UnitEngine`).
   - Create `com.example.domain.models` (`CalculationResult`, `CalculationStep`, `EngineeringAssumption`).
2. **Step 2: Safe Expression Parser & Scientific Calculator**:
   - Create `ExpressionEvaluator` in `com.example.domain.calculators.scientific`.
   - Build `ScientificCalculatorScreen` with quick-tab keypad.
3. **Step 3: Unit Converter & Math Calculators**:
   - Build `UnitConverterScreen`.
   - Implement `QuadraticEquationSolver` and `StatisticsCalculator`.
4. **Step 4: Mechanical & Physical Calculators**:
   - Implement `AxialStressCalculator` ($\sigma = F/A$).
   - Implement `MassVolumeCalculator` with material density presets.
   - Implement `BeamBendingCalculator` (point & distributed loads).
5. **Step 5: Electrical & Industrial Calculators**:
   - Implement `OhmsLawCalculator` ($V, I, R, P$ multivariable solver).
   - Implement `ThreePhasePowerCalculator` ($P, Q, S$, power factor).
   - Implement `OeeTrsCalculator` (Availability, Performance, Quality, OEE).
6. **Step 6: Generic Calculator Screen & UI Integration**:
   - Build reusable `StandardCalculatorScreen` with inputs, unit selectors, calculate/reset, result card, and accordion steps.
   - Update `ToolCatalog` with `isAvailable = true` for the 10 calculators.
   - Wire Navigation routes in `EngineerKitApp.kt` and `EngineerKitDestinations.kt`.
7. **Step 7: Testing & Build Verification**:
   - Write comprehensive JUnit tests in `app/src/test/java/com/example/domain/` for all 10 engines and the unit conversion system.
   - Execute tests with `gradle :app:testDebugUnitTest` and verify build with `compile_applet`.
