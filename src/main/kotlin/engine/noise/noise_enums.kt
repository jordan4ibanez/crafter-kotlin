package engine.noise

enum class NoiseType { Value, ValueFractal, Perlin, PerlinFractal, Simplex, SimplexFractal, Cellular, WhiteNoise, Cubic, CubicFractal }
enum class Interp { Linear, Hermite, Quintic }
enum class FractalType { FBM, Billow, RigidMulti }
enum class CellularDistanceFunction { Euclidean, Manhattan, Natural }
enum class CellularReturnType { CellValue, NoiseLookup, Distance, Distance2, Distance2Add, Distance2Sub, Distance2Mul, Distance2Div }

internal val Value = NoiseType.Value
internal val ValueFractal = NoiseType.ValueFractal
internal val Perlin = NoiseType.Perlin
internal val PerlinFractal = NoiseType.PerlinFractal
internal val Simplex = NoiseType.Simplex
internal val SimplexFractal = NoiseType.SimplexFractal
internal val Cellular = NoiseType.Cellular
internal val WhiteNoise = NoiseType.WhiteNoise
internal val Cubic = NoiseType.Cubic
internal val CubicFractal = NoiseType.CubicFractal

internal val Linear = Interp.Linear
internal val Hermite = Interp.Hermite
internal val Quintic = Interp.Quintic

internal val FBM = FractalType.FBM
internal val Billow = FractalType.Billow
internal val RigidMulti = FractalType.RigidMulti

internal val Euclidean = CellularDistanceFunction.Euclidean
internal val Manhattan = CellularDistanceFunction.Manhattan
internal val Natural = CellularDistanceFunction.Natural

internal val CellValue = CellularReturnType.CellValue
internal val NoiseLookup = CellularReturnType.NoiseLookup
internal val Distance = CellularReturnType.Distance
internal val Distance2 = CellularReturnType.Distance2
internal val Distance2Add = CellularReturnType.Distance2Add
internal val Distance2Sub = CellularReturnType.Distance2Sub
internal val Distance2Mul = CellularReturnType.Distance2Mul
internal val Distance2Div = CellularReturnType.Distance2Div