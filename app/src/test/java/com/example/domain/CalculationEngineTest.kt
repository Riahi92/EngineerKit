package com.example.domain

import com.example.core.data.ToolCatalog
import com.example.domain.calculators.*
import com.example.domain.models.EngineOutcome
import com.example.domain.units.UnitEngine
import com.example.domain.units.UnitRegistry
import org.junit.Assert.*
import org.junit.Test

class CalculationEngineTest {

    private val eps = 1e-4

    // =========================================================================
    // 1. UNIT CONVERSION & CATALOG INTEGRITY TESTS
    // =========================================================================

    @Test
    fun testAll30ToolsRegisteredAndAvailable() {
        assertEquals(30, ToolCatalog.tools.size)
        assertTrue(ToolCatalog.tools.all { it.isAvailable })
    }

    @Test
    fun testLengthConversions() {
        assertEquals(1000.0, UnitEngine.convert(1.0, UnitRegistry.M, UnitRegistry.MM), eps)
        assertEquals(100.0, UnitEngine.convert(1.0, UnitRegistry.M, UnitRegistry.CM), eps)
        assertEquals(1.0, UnitEngine.convert(1000.0, UnitRegistry.M, UnitRegistry.KM), eps)
        assertEquals(25.4, UnitEngine.convert(1.0, UnitRegistry.INCH, UnitRegistry.MM), eps)
        assertEquals(12.0, UnitEngine.convert(1.0, UnitRegistry.FT, UnitRegistry.INCH), eps)
    }

    @Test
    fun testPressureConversions() {
        assertEquals(100000.0, UnitEngine.convert(1.0, UnitRegistry.BAR, UnitRegistry.PA), eps)
        assertEquals(1.0, UnitEngine.convert(10.0, UnitRegistry.BAR, UnitRegistry.MPA), eps)
        assertEquals(14.5038, UnitEngine.convert(1.0, UnitRegistry.BAR, UnitRegistry.PSI), 1e-2)
    }

    @Test
    fun testTemperatureAffineConversions() {
        assertEquals(273.15, UnitEngine.convert(0.0, UnitRegistry.CELSIUS, UnitRegistry.KELVIN), eps)
        assertEquals(32.0, UnitEngine.convert(0.0, UnitRegistry.CELSIUS, UnitRegistry.FAHRENHEIT), eps)
        assertEquals(373.15, UnitEngine.convert(100.0, UnitRegistry.CELSIUS, UnitRegistry.KELVIN), eps)
        assertEquals(212.0, UnitEngine.convert(100.0, UnitRegistry.CELSIUS, UnitRegistry.FAHRENHEIT), eps)
        assertEquals(-40.0, UnitEngine.convert(-40.0, UnitRegistry.CELSIUS, UnitRegistry.FAHRENHEIT), eps)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testIncompatibleDimensionalConversionThrows() {
        UnitEngine.convert(10.0, UnitRegistry.M, UnitRegistry.KG)
    }

    // =========================================================================
    // PHASE 2 CALCULATORS (Tools 1–10)
    // =========================================================================

    @Test
    fun testScientificEvaluatorArithmeticAndPrecedence() {
        val evalDeg = ScientificEvaluator(AngleMode.DEG)
        assertEquals(14.0, evalDeg.evaluate("2 + 3 * 4"), eps)
        assertEquals(20.0, evalDeg.evaluate("(2 + 3) * 4"), eps)
        assertEquals(8.0, evalDeg.evaluate("2 ^ 3"), eps)
        assertEquals(4.0, evalDeg.evaluate("sqrt(16)"), eps)
        assertEquals(3.0, evalDeg.evaluate("cbrt(27)"), eps)
    }

    @Test
    fun testScientificEvaluatorTrigAndLogs() {
        val evalDeg = ScientificEvaluator(AngleMode.DEG)
        assertEquals(1.0, evalDeg.evaluate("sin(90)"), eps)
        assertEquals(1.0, evalDeg.evaluate("cos(0)"), eps)
        assertEquals(1.0, evalDeg.evaluate("tan(45)"), eps)

        val evalRad = ScientificEvaluator(AngleMode.RAD)
        assertEquals(0.0, evalRad.evaluate("sin(0)"), eps)
        assertEquals(1.0, evalRad.evaluate("ln(e)"), eps)
        assertEquals(2.0, evalRad.evaluate("log(100)"), eps)
    }

    @Test
    fun testQuadraticTwoRealRoots() {
        val outcome = QuadraticEquationSolver.solve(1.0, -5.0, 6.0)
        assertTrue(outcome is EngineOutcome.Success)
        val res = (outcome as EngineOutcome.Success).result
        val roots = res.outputs.filter { it.label.contains("racine", ignoreCase = true) }
        assertEquals(2, roots.size)
    }

    @Test
    fun testStatisticsCalculations() {
        val dataset = listOf(10.0, 20.0, 30.0, 40.0, 50.0)
        val outcome = StatisticsCalculator.calculate(dataset)
        assertTrue(outcome is EngineOutcome.Success)
        val res = (outcome as EngineOutcome.Success).result
        assertEquals("30", res.outputs.find { it.label.contains("Moyenne") }?.valueFormatted)
        assertEquals("30", res.outputs.find { it.label.contains("Médiane") }?.valueFormatted)
        assertEquals("40", res.outputs.find { it.label.contains("Étendue") }?.valueFormatted)
    }

    @Test
    fun testAxialStressTension() {
        val outcome = AxialStressCalculator.calculate(100.0, UnitRegistry.KN, 1000.0, UnitRegistry.MM2, 250.0)
        assertTrue(outcome is EngineOutcome.Success)
        val res = (outcome as EngineOutcome.Success).result
        assertEquals("100", res.outputs.find { it.label.contains("Contrainte axiale") }?.valueFormatted)
    }

    @Test
    fun testMassVolumePrism() {
        val outcome = MassVolumeCalculator.calculatePrism(1.0, 1.0, 1.0, UnitRegistry.M, 1000.0)
        assertTrue(outcome is EngineOutcome.Success)
        val res = (outcome as EngineOutcome.Success).result
        assertEquals("1000", res.outputs.find { it.label == "Masse totale" }?.valueFormatted)
    }

    @Test
    fun testBeamBendingCentralLoad() {
        val outcome = BeamBendingCalculator.calculate(BeamLoadType.CENTRAL_POINT_LOAD, 4.0, 10.0, 210.0, 1000.0)
        assertTrue(outcome is EngineOutcome.Success)
        val res = (outcome as EngineOutcome.Success).result
        assertEquals("10", res.outputs.find { it.label.contains("Moment") }?.valueFormatted)
    }

    @Test
    fun testOhmsLawCalculation() {
        val outcome = OhmsLawCalculator.calculate(OhmsLawPair.VOLTAGE_CURRENT, 230.0, 10.0)
        assertTrue(outcome is EngineOutcome.Success)
        val res = (outcome as EngineOutcome.Success).result
        assertEquals("23", res.outputs.find { it.label.contains("Résistance") }?.valueFormatted)
        assertEquals("2300", res.outputs.find { it.label.contains("Puissance") }?.valueFormatted)
    }

    @Test
    fun testThreePhasePowerCalculation() {
        val outcome = ThreePhasePowerCalculator.calculate(400.0, 10.0, 1.0)
        assertTrue(outcome is EngineOutcome.Success)
        val res = (outcome as EngineOutcome.Success).result
        assertEquals("6.9282", res.outputs.find { it.label.contains("Puissance active") }?.valueFormatted)
    }

    @Test
    fun testOeeCalculation() {
        val outcome = OeeTrsCalculator.calculate(8.0, 7.2, 20.0, 1000.0, 980.0)
        assertTrue(outcome is EngineOutcome.Success)
        val res = (outcome as EngineOutcome.Success).result
        assertEquals("90", res.outputs.find { it.label == "Taux de Disponibilité" }?.valueFormatted)
        assertEquals("98", res.outputs.find { it.label == "Taux de Qualité" }?.valueFormatted)
    }

    // =========================================================================
    // PHASE 3 CALCULATORS (Tools 11–30)
    // =========================================================================

    // Tool 11: Circular Shaft Torsion
    @Test
    fun testShaftTorsionSolidAndInvalidDiameter() {
        val valid = ShaftTorsionCalculator.calculate(
            torqueValue = 500.0,
            torqueUnit = UnitRegistry.NM,
            lengthValue = 1.0,
            lengthUnit = UnitRegistry.M,
            outerDiameterValue = 40.0,
            innerDiameterValue = 0.0,
            diameterUnit = UnitRegistry.MM,
            shearModulusGpa = 80.0
        )
        assertTrue(valid is EngineOutcome.Success)
        val res = (valid as EngineOutcome.Success).result
        assertNotNull(res.outputs.find { it.label.contains("Contrainte") })

        // Invalid: d >= D
        val invalid = ShaftTorsionCalculator.calculate(
            500.0, UnitRegistry.NM, 1.0, UnitRegistry.M, 40.0, 40.0, UnitRegistry.MM, 80.0
        )
        assertTrue(invalid is EngineOutcome.Failure)
    }

    // Tool 12: Voltage Drop
    @Test
    fun testVoltageDropSingleAndThreePhase() {
        // DC 2-wire: L=50m, I=20A, rho=0.0175, S=4mm² => dU = 2 * 0.0175 * 50 * 20 / 4 = 8.75 V
        val outcome = VoltageDropCalculator.calculate(
            circuitType = VoltageDropCircuitType.DC_TWO_WIRE,
            currentAmps = 20.0,
            lengthMeters = 50.0,
            areaMm2 = 4.0,
            resistivityOhmMm2PerM = 0.0175,
            nominalVoltageV = 230.0
        )
        assertTrue(outcome is EngineOutcome.Success)
        val res = (outcome as EngineOutcome.Success).result
        assertEquals("8.75", res.outputs.first().valueFormatted)
    }

    // Tool 13: Equivalent Resistance
    @Test
    fun testEquivalentResistanceSeriesAndParallel() {
        val series = EquivalentResistanceCalculator.calculate(
            ResistanceConnectionMode.SERIES,
            listOf(100.0, 200.0, 300.0)
        )
        assertTrue(series is EngineOutcome.Success)
        assertEquals("600", (series as EngineOutcome.Success).result.outputs.first().valueFormatted)

        val parallel = EquivalentResistanceCalculator.calculate(
            ResistanceConnectionMode.PARALLEL,
            listOf(100.0, 100.0)
        )
        assertTrue(parallel is EngineOutcome.Success)
        assertEquals("50", (parallel as EngineOutcome.Success).result.outputs.first().valueFormatted)
    }

    // Tool 14: Voltage Divider
    @Test
    fun testVoltageDivider() {
        // Vin = 12V, R1 = 1k, R2 = 1k => Vout = 6V, ratio = 0.5
        val outcome = VoltageDividerCalculator.calculate(12.0, 1.0, 1.0, "kΩ")
        assertTrue(outcome is EngineOutcome.Success)
        val res = (outcome as EngineOutcome.Success).result
        assertEquals("6", res.outputs.first().valueFormatted)
        assertEquals("0.5", res.outputs[1].valueFormatted)
    }

    // Tool 15: RC Time Constant
    @Test
    fun testRcTimeConstant() {
        // R = 1000 Ohm, C = 0.001 F => tau = 1 s = 1000 ms, 5*tau = 5000 ms
        val outcome = RcTimeConstantCalculator.calculate(
            resistanceOhms = 1000.0,
            capacitanceFarads = 0.001,
            supplyVoltageV = 10.0,
            evaluationTimeSeconds = 1.0
        )
        assertTrue(outcome is EngineOutcome.Success)
        val res = (outcome as EngineOutcome.Success).result
        assertEquals("1000", res.outputs.first().valueFormatted)
        assertEquals("1", res.outputs[1].valueFormatted)
    }

    // Tool 16: Concrete Volume
    @Test
    fun testConcreteVolumeSlabAndCylinder() {
        // Slab 10m x 5m x 0.2m = 10 m³, with 5% allowance = 10.5 m³
        val slab = ConcreteVolumeCalculator.calculate(
            ConcreteElementType.RECTANGULAR_SLAB,
            10.0, 5.0, 0.2, UnitRegistry.M, 5.0, 1
        )
        assertTrue(slab is EngineOutcome.Success)
        val res = (slab as EngineOutcome.Success).result
        assertEquals("10", res.outputs[0].valueFormatted)
        assertEquals("10.5", res.outputs[1].valueFormatted)
    }

    // Tool 17: Distributed Load
    @Test
    fun testDistributedLoadReduction() {
        // q = 15 kN/m, L = 6 m => F = 90 kN, x_c = 3 m
        val outcome = DistributedLoadCalculator.calculate(15.0, true, 6.0, UnitRegistry.M)
        assertTrue(outcome is EngineOutcome.Success)
        val res = (outcome as EngineOutcome.Success).result
        assertEquals("90", res.outputs[0].valueFormatted)
        assertEquals("3", res.outputs[2].valueFormatted)
    }

    // Tool 18: Hydraulic Cylinder Force
    @Test
    fun testHydraulicCylinderForce() {
        // P = 100 bar = 10 MPa, D = 100 mm, d = 50 mm, eta = 1.0 (100%)
        val outcome = HydraulicCylinderForceCalculator.calculate(
            100.0, UnitRegistry.BAR, 100.0, 50.0, UnitRegistry.MM, 1.0
        )
        assertTrue(outcome is EngineOutcome.Success)
        val res = (outcome as EngineOutcome.Success).result
        // F_ext = 10 MPa * pi * 50^2 = 78539.8 N = 78.54 kN
        assertEquals("78.54", res.outputs[0].valueFormatted)
    }

    // Tool 19: Hydraulic Cylinder Flow & Speed
    @Test
    fun testHydraulicCylinderFlow() {
        val outcome = HydraulicCylinderFlowCalculator.calculate(
            HydraulicSolveTarget.SOLVE_VELOCITY_FROM_FLOW,
            pistonDiameterMm = 80.0,
            rodDiameterMm = 45.0,
            knownValue = 30.0 // L/min
        )
        assertTrue(outcome is EngineOutcome.Success)
        val res = (outcome as EngineOutcome.Success).result
        assertEquals("0.0995", res.outputs[0].valueFormatted)
    }

    // Tool 20: Heat Conduction (Fourier)
    @Test
    fun testHeatConductionFourier() {
        // k = 2.0 W/(m·K), A = 10 m², L = 0.2 m, dT = 20 K => Q = 2 * 10 * 20 / 0.2 = 2000 W
        val outcome = HeatConductionCalculator.calculate(
            2.0, 10.0, UnitRegistry.M2, 0.2, UnitRegistry.M, 20.0
        )
        assertTrue(outcome is EngineOutcome.Success)
        val res = (outcome as EngineOutcome.Success).result
        assertEquals("2000", res.outputs[0].valueFormatted)
    }

    // Tool 21: Linear Thermal Expansion
    @Test
    fun testThermalExpansion() {
        // L0 = 10 m, alpha = 12e-6 /K, dT = 100 K => dL = 12e-6 * 10 * 100 = 0.012 m = 12 mm
        val outcome = ThermalExpansionCalculator.calculate(
            10.0, UnitRegistry.M, 12.0, 100.0
        )
        assertTrue(outcome is EngineOutcome.Success)
        val res = (outcome as EngineOutcome.Success).result
        assertEquals("12", res.outputs[0].valueFormatted)
    }

    // Tool 22: Takt Time
    @Test
    fun testTaktTime() {
        // Available = 480 min, Breaks = 30 min (Net = 450 min = 27000 s), Demand = 900 units => Takt = 30 s/unit
        val outcome = TaktTimeCalculator.calculate(480.0, 30.0, 900.0)
        assertTrue(outcome is EngineOutcome.Success)
        val res = (outcome as EngineOutcome.Success).result
        assertEquals("30", res.outputs[0].valueFormatted)
    }

    // Tool 23: Production Capacity
    @Test
    fun testProductionCapacity() {
        // 8h = 28800s, cycle = 28.8s (1000 units/machine), 2 machines = 2000 theo, A=90%, P=100% => 1800 adjusted
        val outcome = ProductionCapacityCalculator.calculate(8.0, 28.8, 2, 90.0, 100.0)
        assertTrue(outcome is EngineOutcome.Success)
        val res = (outcome as EngineOutcome.Success).result
        assertEquals("2000", res.outputs[0].valueFormatted)
        assertEquals("1800", res.outputs[1].valueFormatted)
    }

    // Tool 24: Molar Concentration
    @Test
    fun testMolarConcentration() {
        // 58.44 g NaCl (M=58.44 g/mol) in 1 L => 1 mol/L
        val outcome = MolarConcentrationCalculator.calculate(
            SoluteInputMode.FROM_MASS_AND_MOLAR_MASS,
            58.44, UnitRegistry.G, 58.44, 1.0, UnitRegistry.L
        )
        assertTrue(outcome is EngineOutcome.Success)
        val res = (outcome as EngineOutcome.Success).result
        assertEquals("1", res.outputs[0].valueFormatted)
    }

    // Tool 25: Solution Dilution
    @Test
    fun testSolutionDilution() {
        // C1=2.0 M, C2=0.2 M, V2=250 mL => V1 = 25 mL
        val outcome = SolutionDilutionCalculator.calculate(
            DilutionUnknown.SOLVE_V1, c1 = 2.0, v1 = 0.0, c2 = 0.2, v2 = 250.0
        )
        assertTrue(outcome is EngineOutcome.Success)
        val res = (outcome as EngineOutcome.Success).result
        assertEquals("25", res.outputs[1].valueFormatted)
    }

    // Tool 26: Number Base Converter
    @Test
    fun testNumberBaseConverter() {
        val outcome = NumberBaseConverter.convert("255", NumberBase.DECIMAL)
        assertTrue(outcome is EngineOutcome.Success)
        val res = (outcome as EngineOutcome.Success).result
        assertEquals("0xFF", res.outputs[1].valueFormatted)
        assertEquals("11111111", res.outputs[2].valueFormatted)
        assertEquals("0o377", res.outputs[3].valueFormatted)

        // Invalid binary character should fail
        val invalid = NumberBaseConverter.convert("10201", NumberBase.BINARY)
        assertTrue(invalid is EngineOutcome.Failure)
    }

    // Tool 27: Boolean Logic
    @Test
    fun testBooleanLogicGates() {
        assertTrue(BooleanLogicCalculator.evaluateGate(LogicGate.AND, true, true))
        assertFalse(BooleanLogicCalculator.evaluateGate(LogicGate.AND, true, false))
        assertTrue(BooleanLogicCalculator.evaluateGate(LogicGate.XOR, true, false))
        assertFalse(BooleanLogicCalculator.evaluateGate(LogicGate.XOR, true, true))
        assertTrue(BooleanLogicCalculator.evaluateGate(LogicGate.NAND, true, false))
        assertFalse(BooleanLogicCalculator.evaluateGate(LogicGate.NOR, true, false))
        assertFalse(BooleanLogicCalculator.evaluateGate(LogicGate.NOT, true, false))
    }

    // Tool 28: Analog Signal Scaling
    @Test
    fun testAnalogSignalScaling() {
        // X = 5 V in [0..10 V] -> Y in [0..100 °C] => Y = 50 °C
        val outcome = AnalogSignalScalingCalculator.calculate(5.0, 0.0, 10.0, 0.0, 100.0, "V", "°C")
        assertTrue(outcome is EngineOutcome.Success)
        val res = (outcome as EngineOutcome.Success).result
        assertEquals("50", res.outputs[0].valueFormatted)
    }

    // Tool 29: 4–20 mA Current Loop Converter
    @Test
    fun testCurrentLoop4To20mA() {
        // I = 12 mA (50%) on [0..16 bar] => 8 bar
        val outcome = CurrentLoop4To20mACalculator.calculate(12.0, 0.0, 16.0, "bar")
        assertTrue(outcome is EngineOutcome.Success)
        val res = (outcome as EngineOutcome.Success).result
        assertEquals("8", res.outputs[0].valueFormatted)
        assertEquals("50", res.outputs[1].valueFormatted)

        // Under-range warning check (< 4 mA)
        val underRange = CurrentLoop4To20mACalculator.calculate(3.2, 0.0, 16.0, "bar")
        assertTrue(underRange is EngineOutcome.Success)
        assertTrue((underRange as EngineOutcome.Success).result.warnings.isNotEmpty())
    }

    // Tool 30: Energy and CO2e
    @Test
    fun testEnergyAndCo2Calculator() {
        // P = 10 kW, t = 100 h => E = 1000 kWh; EF = 0.05 kg CO2e/kWh => 50 kg CO2e
        val outcome = EnergyCo2Calculator.calculate(10.0, UnitRegistry.KW, 100.0, UnitRegistry.HOUR, 0.05)
        assertTrue(outcome is EngineOutcome.Success)
        val res = (outcome as EngineOutcome.Success).result
        assertEquals("1000", res.outputs[0].valueFormatted)
        assertEquals("50", res.outputs[1].valueFormatted)
    }
}
