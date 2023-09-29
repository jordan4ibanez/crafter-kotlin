package engine

import org.joml.Math.*
import org.joml.Vector2f
import org.joml.Vector3f
import java.lang.RuntimeException

//note: IntelliJ's auto translater butchered this so I had to translate it by hand.

// FastNoise.java
//
// MIT License
//
// Copyright(c) 2017 Jordan Peck
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files(the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and / or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions :
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
//
// The developer's email is jorzixdan.me2@gzixmail.com (for great email, take
// off every 'zix'.)
//


enum class NoiseType {Value, ValueFractal, Perlin, PerlinFractal, Simplex, SimplexFractal, Cellular, WhiteNoise, Cubic, CubicFractal}
enum class Interp {Linear, Hermite, Quintic}
enum class FractalType {FBM, Billow, RigidMulti}
enum class CellularDistanceFunction {Euclidean, Manhattan, Natural}
enum class CellularReturnType {CellValue, NoiseLookup, Distance, Distance2, Distance2Add, Distance2Sub, Distance2Mul, Distance2Div}

private val Value          = NoiseType.Value
private val ValueFractal   = NoiseType.ValueFractal
private val Perlin         = NoiseType.Perlin
private val PerlinFractal  = NoiseType.PerlinFractal
private val Simplex        = NoiseType.Simplex
private val SimplexFractal = NoiseType.SimplexFractal
private val Cellular       = NoiseType.Cellular
private val WhiteNoise     = NoiseType.WhiteNoise
private val Cubic          = NoiseType.Cubic
private val CubicFractal   = NoiseType.CubicFractal

private val Linear  = Interp.Linear
private val Hermite = Interp.Hermite
private val Quintic = Interp.Quintic

private val FBM        = FractalType.FBM
private val Billow     = FractalType.Billow
private val RigidMulti = FractalType.RigidMulti

private val Euclidean = CellularDistanceFunction.Euclidean
private val Manhattan = CellularDistanceFunction.Manhattan
private val Natural   = CellularDistanceFunction.Natural

private val CellValue    = CellularReturnType.CellValue
private val NoiseLookup  = CellularReturnType.NoiseLookup
private val Distance     = CellularReturnType.Distance
private val Distance2    = CellularReturnType.Distance2
private val Distance2Add = CellularReturnType.Distance2Add
private val Distance2Sub = CellularReturnType.Distance2Sub
private val Distance2Mul = CellularReturnType.Distance2Mul
private val Distance2Div = CellularReturnType.Distance2Div



class FastNoise {


  private var seed = 1337
  private var frequency = 0.01f
  private var interp = Interp.Quintic
  private var noiseType = NoiseType.Simplex

  private var octaves = 3
  private var lacunarity = 2.0f
  private var gain = 0.5f
  private var fractalType = FractalType.FBM

  private var fractalBounding = 0f

  private var cellularDistanceFunction = CellularDistanceFunction.Euclidean
  private var cellularReturnType = CellularReturnType.CellValue
  private var cellularNoiseLookup: FastNoise? = null

  private var gradientPerturbAmp = (1.0f / 0.45f)

  constructor(seed: Int) {
    this.seed = seed
    calculateFractalBounding()
  }

  // Returns a 0 float/double
  fun getDecimalType(): Float {
    return 0f
  }

  // Returns the seed used by this object
  fun getSeed(): Int {
    return seed
  }

  // Sets seed used for all noise types
  // Default: 1337
  fun setSeed(seed: Int) {
    this.seed = seed
  }

  // Sets frequency for all noise types
  // Default: 0.01
  fun setFrequency(frequency: Float) {
    this.frequency = frequency
  }

  // Changes the interpolation method used to smooth between noise values
  // Possible interpolation methods (lowest to highest quality) :
  // - Linear
  // - Hermite
  // - Quintic
  // Used in Value, Gradient Noise and Position Perturbing
  // Default: Quintic
  fun setInterp(interp: Interp) {
    this.interp = interp
  }

  // Sets noise return type of GetNoise(...)
  // Default: Simplex
  fun setNoiseType(noiseType: NoiseType) {
    this.noiseType = noiseType
  }

  // Sets octave count for all fractal noise types
  // Default: 3
  fun setFractalOctaves(octaves: Int) {
    this.octaves = octaves
    calculateFractalBounding()
  }

  // Sets octave lacunarity for all fractal noise types
  // Default: 2.0
  fun setFractalLacunarity(lacunarity: Float) {
    this.lacunarity = lacunarity
  }

  // Sets octave gain for all fractal noise types
  // Default: 0.5
  fun setFractalGain(gain: Float) {
    this.gain = gain
    calculateFractalBounding()
  }

  // Sets method for combining octaves in all fractal noise types
  // Default: FBM
  fun setFractalType(fractalType: FractalType) {
    this.fractalType = fractalType
  }

  // Sets return type from cellular noise calculations
  // Note: NoiseLookup requires another FastNoise object be set with SetCellularNoiseLookup() to function
  // Default: CellValue
  fun setCellularDistanceFunction(cellularDistanceFunction: CellularDistanceFunction) {
    this.cellularDistanceFunction = cellularDistanceFunction
  }

  // Sets distance function used in cellular noise calculations
  // Default: Euclidean
  fun setCellularReturnType(cellularReturnType: CellularReturnType) {
    this.cellularReturnType = cellularReturnType
  }

  // Noise used to calculate a cell value if cellular return type is NoiseLookup
  // The lookup value is acquired through GetNoise() so ensure you SetNoiseType() on the noise lookup, value, gradient or simplex is recommended
  fun setCellularNoiseLookup(noise: FastNoise) {
    cellularNoiseLookup = noise
  }

  // Sets the maximum perturb distance from original location when using GradientPerturb{Fractal}(...)
  // Default: 1.0
  fun setGradientPerturbAmp(gradientPerturbAmp: Float) {
    this.gradientPerturbAmp = gradientPerturbAmp / 0.45f
  }

  @JvmRecord
  private data class Float2(val x: Float, val y: Float)

  @JvmRecord
  private data class Float3(val x: Float, val y: Float, val z: Float)

  private val GRAD_2D = arrayOf(
    Float2(-1f, -1f), Float2(1f, -1f), Float2(-1f, 1f), Float2(1f, 1f),
    Float2(0f, -1f), Float2(-1f, 0f), Float2(0f, 1f), Float2(1f, 0f),
  )

  private val GRAD_3D = arrayOf(
    Float3(1f, 1f, 0f), Float3(-1f, 1f, 0f), Float3(1f, -1f, 0f), Float3(-1f, -1f, 0f),
    Float3(1f, 0f, 1f), Float3(-1f, 0f, 1f), Float3(1f, 0f, -1f), Float3(-1f, 0f, -1f),
    Float3(0f, 1f, 1f), Float3(0f, -1f, 1f), Float3(0f, 1f, -1f), Float3(0f, -1f, -1f),
    Float3(1f, 1f, 0f), Float3(0f, -1f, 1f), Float3(-1f, 1f, 0f), Float3(0f, -1f, -1f),
  )

  private val CELL_2D =
    arrayOf(
      Float2(-0.43135393f, 0.12819435f),
      Float2(-0.17333168f, 0.41527838f),
      Float2(-0.28219575f, -0.35052183f),
      Float2(-0.28064737f, 0.35176277f),
      Float2(0.3125509f, -0.3237467f),
      Float2(0.33830184f, -0.29673535f),
      Float2(-0.4393982f, -0.09710417f),
      Float2(-0.44604436f, -0.05953503f),
      Float2(-0.30222303f, 0.3334085f),
      Float2(-0.21268106f, -0.39656875f),
      Float2(-0.29911566f, 0.33619907f),
      Float2(0.22933237f, 0.38717782f),
      Float2(0.44754392f, -0.046951506f),
      Float2(0.1777518f, 0.41340572f),
      Float2(0.16885225f, -0.4171198f),
      Float2(-0.097659715f, 0.43927506f),
      Float2(0.084501885f, 0.44199485f),
      Float2(-0.40987605f, -0.18574613f),
      Float2(0.34765857f, -0.2857158f),
      Float2(-0.335067f, -0.30038327f),
      Float2(0.229819f, -0.38688916f),
      Float2(-0.010699241f, 0.4498728f),
      Float2(-0.44601414f, -0.059761196f),
      Float2(0.3650294f, 0.26316068f),
      Float2(-0.34947944f, 0.28348568f),
      Float2(-0.41227207f, 0.18036559f),
      Float2(-0.26732782f, 0.36198872f),
      Float2(0.32212403f, -0.31422302f),
      Float2(0.2880446f, -0.34573156f),
      Float2(0.38921708f, -0.22585405f),
      Float2(0.4492085f, -0.026678115f),
      Float2(-0.44977248f, 0.014307996f),
      Float2(0.12781754f, -0.43146574f),
      Float2(-0.035721004f, 0.44858f),
      Float2(-0.4297407f, -0.13350253f),
      Float2(-0.32178178f, 0.3145735f),
      Float2(-0.3057159f, 0.33020872f),
      Float2(-0.414504f, 0.17517549f),
      Float2(-0.373814f, 0.25052565f),
      Float2(0.22368914f, -0.39046532f),
      Float2(0.0029677756f, -0.4499902f),
      Float2(0.17471284f, -0.4146992f),
      Float2(-0.44237724f, -0.08247648f),
      Float2(-0.2763961f, -0.35511294f),
      Float2(-0.4019386f, -0.20234962f),
      Float2(0.3871414f, -0.22939382f),
      Float2(-0.43000874f, 0.1326367f),
      Float2(-0.030375743f, -0.44897363f),
      Float2(-0.34861815f, 0.28454417f),
      Float2(0.045535173f, -0.44769025f),
      Float2(-0.037580293f, 0.44842806f),
      Float2(0.3266409f, 0.309525f),
      Float2(0.065400176f, -0.4452222f),
      Float2(0.03409026f, 0.44870687f),
      Float2(-0.44491938f, 0.06742967f),
      Float2(-0.4255936f, -0.14618507f),
      Float2(0.4499173f, 0.008627303f),
      Float2(0.052426063f, 0.44693568f),
      Float2(-0.4495305f, -0.020550266f),
      Float2(-0.12047757f, 0.43357256f),
      Float2(-0.3419864f, -0.2924813f),
      Float2(0.386532f, 0.23041917f),
      Float2(0.045060977f, -0.4477382f),
      Float2(-0.06283466f, 0.4455915f),
      Float2(0.39326003f, -0.21873853f),
      Float2(0.44722617f, -0.04988731f),
      Float2(0.3753571f, -0.24820767f),
      Float2(-0.2736623f, 0.35722396f),
      Float2(0.17004615f, 0.4166345f),
      Float2(0.41026923f, 0.18487608f),
      Float2(0.3232272f, -0.31308815f),
      Float2(-0.28823102f, -0.34557614f),
      Float2(0.20509727f, 0.4005435f),
      Float2(0.4414086f, -0.08751257f),
      Float2(-0.16847004f, 0.4172743f),
      Float2(-0.0039780326f, 0.4499824f),
      Float2(-0.20551336f, 0.4003302f),
      Float2(-0.006095675f, -0.4499587f),
      Float2(-0.11962281f, -0.43380916f),
      Float2(0.39015284f, -0.2242337f),
      Float2(0.017235318f, 0.4496698f),
      Float2(-0.30150703f, 0.33405614f),
      Float2(-0.015142624f, -0.44974515f),
      Float2(-0.4142574f, -0.1757578f),
      Float2(-0.19163772f, -0.40715474f),
      Float2(0.37492487f, 0.24886008f),
      Float2(-0.22377743f, 0.39041474f),
      Float2(-0.41663432f, -0.17004661f),
      Float2(0.36191717f, 0.2674247f),
      Float2(0.18911268f, -0.4083337f),
      Float2(-0.3127425f, 0.3235616f),
      Float2(-0.3281808f, 0.30789182f),
      Float2(-0.22948067f, 0.38708994f),
      Float2(-0.34452662f, 0.28948474f),
      Float2(-0.41670954f, -0.16986217f),
      Float2(-0.2578903f, -0.36877173f),
      Float2(-0.3612038f, 0.26838747f),
      Float2(0.22679965f, 0.38866684f),
      Float2(0.20715706f, 0.3994821f),
      Float2(0.083551764f, -0.44217542f),
      Float2(-0.43122333f, 0.12863296f),
      Float2(0.32570556f, 0.3105091f),
      Float2(0.1777011f, -0.41342753f),
      Float2(-0.44518253f, 0.0656698f),
      Float2(0.39551434f, 0.21463552f),
      Float2(-0.4264614f, 0.14363383f),
      Float2(-0.37937996f, -0.24201414f),
      Float2(0.04617599f, -0.4476246f),
      Float2(-0.37140542f, -0.25408268f),
      Float2(0.25635704f, -0.36983925f),
      Float2(0.03476646f, 0.44865498f),
      Float2(-0.30654544f, 0.32943875f),
      Float2(-0.22569798f, 0.38930762f),
      Float2(0.41164485f, -0.18179253f),
      Float2(-0.29077458f, -0.3434387f),
      Float2(0.28422785f, -0.3488761f),
      Float2(0.31145895f, -0.32479736f),
      Float2(0.44641557f, -0.05668443f),
      Float2(-0.3037334f, -0.33203316f),
      Float2(0.4079607f, 0.18991591f),
      Float2(-0.3486949f, -0.2844501f),
      Float2(0.32648215f, 0.30969244f),
      Float2(0.32111424f, 0.3152549f),
      Float2(0.011833827f, 0.44984436f),
      Float2(0.43338442f, 0.1211526f),
      Float2(0.31186685f, 0.32440573f),
      Float2(-0.27275348f, 0.35791835f),
      Float2(-0.42222863f, -0.15563737f),
      Float2(-0.10097001f, -0.438526f),
      Float2(-0.2741171f, -0.35687506f),
      Float2(-0.14651251f, 0.425481f),
      Float2(0.2302279f, -0.38664597f),
      Float2(-0.36994356f, 0.25620648f),
      Float2(0.10570035f, -0.4374099f),
      Float2(-0.26467136f, 0.36393553f),
      Float2(0.3521828f, 0.2801201f),
      Float2(-0.18641879f, -0.40957054f),
      Float2(0.1994493f, -0.40338564f),
      Float2(0.3937065f, 0.21793391f),
      Float2(-0.32261583f, 0.31371805f),
      Float2(0.37962353f, 0.2416319f),
      Float2(0.1482922f, 0.424864f),
      Float2(-0.4074004f, 0.19111493f),
      Float2(0.4212853f, 0.15817298f),
      Float2(-0.26212972f, 0.36577043f),
      Float2(-0.2536987f, -0.37166783f),
      Float2(-0.21002364f, 0.3979825f),
      Float2(0.36241525f, 0.2667493f),
      Float2(-0.36450386f, -0.26388812f),
      Float2(0.23184867f, 0.38567626f),
      Float2(-0.3260457f, 0.3101519f),
      Float2(-0.21300453f, -0.3963951f),
      Float2(0.3814999f, -0.23865843f),
      Float2(-0.34297732f, 0.29131868f),
      Float2(-0.43558657f, 0.11297941f),
      Float2(-0.21046796f, 0.3977477f),
      Float2(0.33483645f, -0.30064023f),
      Float2(0.34304687f, 0.29123673f),
      Float2(-0.22918367f, -0.38726586f),
      Float2(0.25477073f, -0.3709338f),
      Float2(0.42361748f, -0.1518164f),
      Float2(-0.15387742f, 0.4228732f),
      Float2(-0.44074494f, 0.09079596f),
      Float2(-0.06805276f, -0.4448245f),
      Float2(0.44535172f, -0.06451237f),
      Float2(0.25624645f, -0.36991587f),
      Float2(0.32781982f, -0.30827612f),
      Float2(-0.41227743f, -0.18035334f),
      Float2(0.3354091f, -0.30000123f),
      Float2(0.44663286f, -0.054946158f),
      Float2(-0.16089533f, 0.42025313f),
      Float2(-0.09463955f, 0.43993562f),
      Float2(-0.026376883f, -0.4492263f),
      Float2(0.44710281f, -0.050981198f),
      Float2(-0.4365671f, 0.10912917f),
      Float2(-0.39598587f, 0.21376434f),
      Float2(-0.42400482f, -0.15073125f),
      Float2(-0.38827947f, 0.22746222f),
      Float2(-0.42836526f, -0.13785212f),
      Float2(0.3303888f, 0.30552125f),
      Float2(0.3321435f, -0.30361274f),
      Float2(-0.41302106f, -0.17864382f),
      Float2(0.084030606f, -0.44208467f),
      Float2(-0.38228828f, 0.23739347f),
      Float2(-0.37123957f, -0.25432497f),
      Float2(0.4472364f, -0.049795635f),
      Float2(-0.44665912f, 0.054732345f),
      Float2(0.048627254f, -0.44736493f),
      Float2(-0.42031014f, -0.16074637f),
      Float2(0.22053608f, 0.3922548f),
      Float2(-0.36249006f, 0.2666476f),
      Float2(-0.40360868f, -0.19899757f),
      Float2(0.21527278f, 0.39516786f),
      Float2(-0.43593928f, -0.11161062f),
      Float2(0.4178354f, 0.1670735f),
      Float2(0.20076302f, 0.40273342f),
      Float2(-0.07278067f, -0.4440754f),
      Float2(0.36447486f, -0.26392817f),
      Float2(-0.43174517f, 0.12687041f),
      Float2(-0.29743645f, 0.33768559f),
      Float2(-0.2998672f, 0.3355289f),
      Float2(-0.26736742f, 0.3619595f),
      Float2(0.28084233f, 0.35160714f),
      Float2(0.34989464f, 0.28297302f),
      Float2(-0.22296856f, 0.39087725f),
      Float2(0.33058232f, 0.30531186f),
      Float2(-0.24366812f, -0.37831977f),
      Float2(-0.034027766f, 0.4487116f),
      Float2(-0.31935883f, 0.31703302f),
      Float2(0.44546336f, -0.063737005f),
      Float2(0.44835043f, 0.03849544f),
      Float2(-0.44273585f, -0.08052933f),
      Float2(0.054522987f, 0.44668472f),
      Float2(-0.28125608f, 0.35127628f),
      Float2(0.12666969f, 0.43180412f),
      Float2(-0.37359813f, 0.25084746f),
      Float2(0.29597083f, -0.3389709f),
      Float2(-0.37143773f, 0.25403547f),
      Float2(-0.4044671f, -0.19724695f),
      Float2(0.16361657f, -0.41920117f),
      Float2(0.32891855f, -0.30710354f),
      Float2(-0.2494825f, -0.374511f),
      Float2(0.032831334f, 0.44880074f),
      Float2(-0.16630606f, -0.41814148f),
      Float2(-0.10683318f, 0.43713462f),
      Float2(0.0644026f, -0.4453676f),
      Float2(-0.4483231f, 0.03881238f),
      Float2(-0.42137775f, -0.15792651f),
      Float2(0.05097921f, -0.44710302f),
      Float2(0.20505841f, -0.40056342f),
      Float2(0.41780984f, -0.16713744f),
      Float2(-0.35651895f, -0.27458012f),
      Float2(0.44783983f, 0.04403978f),
      Float2(-0.33999997f, -0.2947881f),
      Float2(0.3767122f, 0.24614613f),
      Float2(-0.31389344f, 0.32244518f),
      Float2(-0.14620018f, -0.42558843f),
      Float2(0.39702904f, -0.21182053f),
      Float2(0.44591492f, -0.0604969f),
      Float2(-0.41048893f, -0.18438771f),
      Float2(0.1475104f, -0.4251361f),
      Float2(0.0925803f, 0.44037357f),
      Float2(-0.15896647f, -0.42098653f),
      Float2(0.2482445f, 0.37533274f),
      Float2(0.43836242f, -0.10167786f),
      Float2(0.06242803f, 0.44564867f),
      Float2(0.2846591f, -0.3485243f),
      Float2(-0.34420276f, -0.28986976f),
      Float2(0.11981889f, -0.43375504f),
      Float2(-0.2435907f, 0.37836963f),
      Float2(0.2958191f, -0.3391033f),
      Float2(-0.1164008f, 0.43468478f),
      Float2(0.12740372f, -0.4315881f),
      Float2(0.3680473f, 0.2589231f),
      Float2(0.2451437f, 0.3773653f),
      Float2(-0.43145096f, 0.12786736f),
    )

  private val CELL_3D =
    arrayOf(
      Float3(0.14537874f, -0.41497818f, -0.09569818f),
      Float3(-0.012428297f, -0.14579184f, -0.42554703f),
      Float3(0.28779796f, -0.026064834f, -0.34495357f),
      Float3(-0.07732987f, 0.23770943f, 0.37418488f),
      Float3(0.11072059f, -0.3552302f, -0.25308585f),
      Float3(0.27552092f, 0.26405212f, -0.23846321f),
      Float3(0.29416895f, 0.15260646f, 0.30442718f),
      Float3(0.4000921f, -0.20340563f, 0.0324415f),
      Float3(-0.16973041f, 0.39708647f, -0.12654613f),
      Float3(-0.14832245f, -0.38596946f, 0.17756131f),
      Float3(0.2623597f, -0.2354853f, 0.27966776f),
      Float3(-0.2709003f, 0.3505271f, -0.07901747f),
      Float3(-0.035165507f, 0.38852343f, 0.22430544f),
      Float3(-0.12677127f, 0.1920044f, 0.38673422f),
      Float3(0.02952022f, 0.44096857f, 0.084706925f),
      Float3(-0.28068542f, -0.26699677f, 0.22897254f),
      Float3(-0.17115955f, 0.21411856f, 0.35687205f),
      Float3(0.21132272f, 0.39024058f, -0.074531786f),
      Float3(-0.10243528f, 0.21280442f, -0.38304216f),
      Float3(-0.330425f, -0.15669867f, 0.26223055f),
      Float3(0.20911114f, 0.31332782f, -0.24616706f),
      Float3(0.34467816f, -0.19442405f, -0.21423413f),
      Float3(0.19844781f, -0.32143423f, -0.24453732f),
      Float3(-0.29290086f, 0.22629151f, 0.2559321f),
      Float3(-0.16173328f, 0.00631477f, -0.41988388f),
      Float3(-0.35820603f, -0.14830318f, -0.2284614f),
      Float3(-0.18520673f, -0.34541193f, -0.2211087f),
      Float3(0.3046301f, 0.10263104f, 0.3149085f),
      Float3(-0.038167685f, -0.25517663f, -0.3686843f),
      Float3(-0.40849522f, 0.18059509f, 0.05492789f),
      Float3(-0.026874434f, -0.27497414f, 0.35519993f),
      Float3(-0.038010985f, 0.3277859f, 0.30596006f),
      Float3(0.23711208f, 0.29003868f, -0.2493099f),
      Float3(0.44476604f, 0.039469305f, 0.05590469f),
      Float3(0.019851472f, -0.015031833f, -0.44931054f),
      Float3(0.4274339f, 0.033459943f, -0.1366773f),
      Float3(-0.20729886f, 0.28714147f, -0.27762738f),
      Float3(-0.3791241f, 0.12811777f, 0.205793f),
      Float3(-0.20987213f, -0.10070873f, -0.38511226f),
      Float3(0.01582799f, 0.42638946f, 0.14297384f),
      Float3(-0.18881294f, -0.31609967f, -0.2587096f),
      Float3(0.1612989f, -0.19748051f, -0.3707885f),
      Float3(-0.08974491f, 0.22914875f, -0.37674487f),
      Float3(0.07041229f, 0.41502303f, -0.15905343f),
      Float3(-0.108292565f, -0.15860616f, 0.40696046f),
      Float3(0.24741006f, -0.33094147f, 0.17823021f),
      Float3(-0.10688367f, -0.27016446f, -0.34363797f),
      Float3(0.23964521f, 0.068036005f, -0.37475494f),
      Float3(-0.30638862f, 0.25974283f, 0.2028785f),
      Float3(0.15933429f, -0.311435f, -0.2830562f),
      Float3(0.27096906f, 0.14126487f, -0.33033317f),
      Float3(-0.15197805f, 0.3623355f, 0.2193528f),
      Float3(0.16997737f, 0.3456013f, 0.232739f),
      Float3(-0.19861557f, 0.38362765f, -0.12602258f),
      Float3(-0.18874821f, -0.2050155f, -0.35333094f),
      Float3(0.26591033f, 0.3015631f, -0.20211722f),
      Float3(-0.08838976f, -0.42888197f, -0.1036702f),
      Float3(-0.042018693f, 0.30995926f, 0.3235115f),
      Float3(-0.32303345f, 0.20154992f, -0.23984788f),
      Float3(0.2612721f, 0.27598545f, -0.24097495f),
      Float3(0.38571304f, 0.21934603f, 0.074918374f),
      Float3(0.07654968f, 0.37217322f, 0.24109592f),
      Float3(0.4317039f, -0.02577753f, 0.12436751f),
      Float3(-0.28904364f, -0.341818f, -0.045980845f),
      Float3(-0.22019476f, 0.38302338f, -0.085483104f),
      Float3(0.41613227f, -0.16696343f, -0.03817252f),
      Float3(0.22047181f, 0.02654239f, -0.391392f),
      Float3(-0.10403074f, 0.38900796f, -0.2008741f),
      Float3(-0.14321226f, 0.3716144f, -0.20950656f),
      Float3(0.39783806f, -0.062066693f, 0.20092937f),
      Float3(-0.25992745f, 0.2616725f, -0.25780848f),
      Float3(0.40326184f, -0.11245936f, 0.1650236f),
      Float3(-0.0895347f, -0.30482447f, 0.31869355f),
      Float3(0.1189372f, -0.2875222f, 0.3250922f),
      Float3(0.02167047f, -0.032846306f, -0.44827616f),
      Float3(-0.34113437f, 0.2500031f, 0.15370683f),
      Float3(0.31629646f, 0.3082064f, -0.08640228f),
      Float3(0.2355139f, -0.34393343f, -0.16953762f),
      Float3(-0.028745415f, -0.39559332f, 0.21255504f),
      Float3(-0.24614552f, 0.020202823f, -0.3761705f),
      Float3(0.042080294f, -0.44704396f, 0.029680781f),
      Float3(0.27274588f, 0.22884719f, -0.27520657f),
      Float3(-0.13475229f, -0.027208483f, -0.42848748f),
      Float3(0.38296244f, 0.123193145f, -0.20165123f),
      Float3(-0.35476136f, 0.12717022f, 0.24591078f),
      Float3(0.23057902f, 0.30638957f, 0.23549682f),
      Float3(-0.08323845f, -0.19222452f, 0.39827263f),
      Float3(0.2993663f, -0.2619918f, -0.21033332f),
      Float3(-0.21548657f, 0.27067477f, 0.2877511f),
      Float3(0.016833553f, -0.26806557f, -0.36105052f),
      Float3(0.052404292f, 0.4335128f, -0.108721785f),
      Float3(0.0094010485f, -0.44728905f, 0.0484161f),
      Float3(0.34656888f, 0.011419145f, -0.28680938f),
      Float3(-0.3706868f, -0.25511044f, 0.0031566927f),
      Float3(0.274117f, 0.21399724f, -0.28559598f),
      Float3(0.06413434f, 0.17087185f, 0.41132662f),
      Float3(-0.38818797f, -0.039732803f, -0.22412363f),
      Float3(0.064194694f, -0.28036824f, 0.3460819f),
      Float3(-0.19861208f, -0.33911735f, 0.21920918f),
      Float3(-0.20320301f, -0.38716415f, 0.10636004f),
      Float3(-0.13897364f, -0.27759016f, -0.32577604f),
      Float3(-0.065556414f, 0.34225327f, -0.28471926f),
      Float3(-0.25292465f, -0.2904228f, 0.23277397f),
      Float3(0.14444765f, 0.1069184f, 0.41255707f),
      Float3(-0.364378f, -0.24471f, -0.09922543f),
      Float3(0.42861426f, -0.13584961f, -0.018295068f),
      Float3(0.16587292f, -0.31368086f, -0.27674988f),
      Float3(0.22196105f, -0.365814f, 0.13933203f),
      Float3(0.043229405f, -0.38327307f, 0.23180372f),
      Float3(-0.0848127f, -0.44048697f, -0.035749655f),
      Float3(0.18220821f, -0.39532593f, 0.1140946f),
      Float3(-0.32693234f, 0.30365425f, 0.05838957f),
      Float3(-0.40804854f, 0.042278584f, -0.18495652f),
      Float3(0.26760253f, -0.012996716f, 0.36155218f),
      Float3(0.30248925f, -0.10099903f, -0.3174893f),
      Float3(0.1448494f, 0.42592168f, -0.01045808f),
      Float3(0.41984022f, 0.0806232f, 0.14047809f),
      Float3(-0.30088723f, -0.3330409f, -0.032413557f),
      Float3(0.36393103f, -0.12912844f, -0.23104121f),
      Float3(0.32958066f, 0.018417599f, -0.30583882f),
      Float3(0.27762595f, -0.2974929f, -0.19215047f),
      Float3(0.41490006f, -0.14479318f, -0.096916884f),
      Float3(0.14501671f, -0.039899293f, 0.4241205f),
      Float3(0.092990234f, -0.29973218f, -0.32251117f),
      Float3(0.10289071f, -0.36126688f, 0.24778973f),
      Float3(0.26830572f, -0.070760414f, -0.35426685f),
      Float3(-0.4227307f, -0.07933162f, -0.13230732f),
      Float3(-0.17812248f, 0.18068571f, -0.3716518f),
      Float3(0.43907887f, -0.028418485f, -0.094351165f),
      Float3(0.29725835f, 0.23827997f, -0.23949975f),
      Float3(-0.17070028f, 0.22158457f, 0.3525077f),
      Float3(0.38066867f, 0.14718525f, -0.18954648f),
      Float3(-0.17514457f, -0.2748879f, 0.31025964f),
      Float3(-0.22272375f, -0.23167789f, 0.31499124f),
      Float3(0.13696331f, 0.13413431f, -0.40712288f),
      Float3(-0.35295033f, -0.24728934f, -0.1295146f),
      Float3(-0.25907442f, -0.29855776f, -0.21504351f),
      Float3(-0.37840194f, 0.21998167f, -0.10449899f),
      Float3(-0.056358058f, 0.14857374f, 0.42101023f),
      Float3(0.32514286f, 0.09666047f, -0.29570064f),
      Float3(-0.41909957f, 0.14067514f, -0.08405979f),
      Float3(-0.3253151f, -0.3080335f, -0.042254567f),
      Float3(0.2857946f, -0.05796152f, 0.34272718f),
      Float3(-0.2733604f, 0.1973771f, -0.29802075f),
      Float3(0.21900366f, 0.24100378f, -0.31057137f),
      Float3(0.31827673f, -0.27134296f, 0.16605099f),
      Float3(-0.03222023f, -0.33311614f, -0.30082467f),
      Float3(-0.30877802f, 0.19927941f, -0.25969952f),
      Float3(-0.06487612f, -0.4311323f, 0.11142734f),
      Float3(0.39211714f, -0.06294284f, -0.2116184f),
      Float3(-0.16064045f, -0.3589281f, -0.21878128f),
      Float3(-0.037677713f, -0.22903514f, 0.3855169f),
      Float3(0.13948669f, -0.3602214f, 0.23083329f),
      Float3(-0.4345094f, 0.005751117f, 0.11691243f),
      Float3(-0.10446375f, 0.41681284f, -0.13362028f),
      Float3(0.26587275f, 0.25519434f, 0.2582393f),
      Float3(0.2051462f, 0.19753908f, 0.3484155f),
      Float3(-0.26608557f, 0.23483312f, 0.2766801f),
      Float3(0.07849406f, -0.33003464f, -0.29566166f),
      Float3(-0.21606864f, 0.053764515f, -0.39105463f),
      Float3(-0.18577918f, 0.21484992f, 0.34903526f),
      Float3(0.024924217f, -0.32299542f, -0.31233433f),
      Float3(-0.12016783f, 0.40172666f, 0.16332598f),
      Float3(-0.021600846f, -0.06885389f, 0.44417626f),
      Float3(0.259767f, 0.30963007f, 0.19786438f),
      Float3(-0.16115539f, -0.09823036f, 0.40850917f),
      Float3(-0.32788968f, 0.14616702f, 0.27133662f),
      Float3(0.2822735f, 0.03754421f, -0.3484424f),
      Float3(0.03169341f, 0.34740525f, -0.28426242f),
      Float3(0.22026137f, -0.3460788f, -0.18497133f),
      Float3(0.2933396f, 0.30319735f, 0.15659896f),
      Float3(-0.3194923f, 0.24537522f, -0.20053846f),
      Float3(-0.3441586f, -0.16988562f, -0.23493347f),
      Float3(0.27036458f, -0.35742772f, 0.040600598f),
      Float3(0.2298569f, 0.37441564f, 0.09735889f),
      Float3(0.09326604f, -0.31701088f, 0.30545956f),
      Float3(-0.11161653f, -0.29850188f, 0.31770802f),
      Float3(0.21729073f, -0.34600052f, -0.1885958f),
      Float3(0.19913395f, 0.38203415f, -0.12998295f),
      Float3(-0.054191817f, -0.21031451f, 0.3941206f),
      Float3(0.08871337f, 0.20121174f, 0.39261147f),
      Float3(0.27876732f, 0.35054046f, 0.04370535f),
      Float3(-0.32216644f, 0.30672136f, 0.06804997f),
      Float3(-0.42773664f, 0.13206677f, 0.045822866f),
      Float3(0.24013188f, -0.1612516f, 0.34472394f),
      Float3(0.1448608f, -0.2387819f, 0.35284352f),
      Float3(-0.38370657f, -0.22063984f, 0.081162356f),
      Float3(-0.4382628f, -0.09082753f, -0.046648555f),
      Float3(-0.37728354f, 0.05445141f, 0.23914887f),
      Float3(0.12595794f, 0.34839457f, 0.25545222f),
      Float3(-0.14062855f, -0.27087736f, -0.33067968f),
      Float3(-0.15806945f, 0.4162932f, -0.06491554f),
      Float3(0.2477612f, -0.29278675f, -0.23535146f),
      Float3(0.29161328f, 0.33125353f, 0.08793625f),
      Float3(0.073652655f, -0.16661598f, 0.4114783f),
      Float3(-0.26126525f, -0.24222377f, 0.27489653f),
      Float3(-0.3721862f, 0.25279015f, 0.008634938f),
      Float3(-0.36911917f, -0.25528118f, 0.032902323f),
      Float3(0.22784418f, -0.3358365f, 0.1944245f),
      Float3(0.36339816f, -0.23101902f, 0.13065979f),
      Float3(-0.3042315f, -0.26984522f, 0.19268309f),
      Float3(-0.3199312f, 0.31633255f, -0.008816978f),
      Float3(0.28748524f, 0.16422755f, -0.30476475f),
      Float3(-0.14510968f, 0.3277541f, -0.27206695f),
      Float3(0.3220091f, 0.05113441f, 0.31015387f),
      Float3(-0.12474009f, -0.043336052f, -0.4301882f),
      Float3(-0.2829556f, -0.30561906f, -0.1703911f),
      Float3(0.10693844f, 0.34910247f, -0.26304305f),
      Float3(-0.14206612f, -0.30553767f, -0.29826826f),
      Float3(-0.25054833f, 0.31564668f, -0.20023163f),
      Float3(0.3265788f, 0.18712291f, 0.24664004f),
      Float3(0.07646097f, -0.30266908f, 0.3241067f),
      Float3(0.34517714f, 0.27571207f, -0.085648015f),
      Float3(0.29813796f, 0.2852657f, 0.17954728f),
      Float3(0.28122503f, 0.34667164f, 0.056844097f),
      Float3(0.43903455f, -0.0979043f, -0.012783354f),
      Float3(0.21483733f, 0.18501726f, 0.3494475f),
      Float3(0.2595421f, -0.07946825f, 0.3589188f),
      Float3(0.3182823f, -0.30735552f, -0.08203022f),
      Float3(-0.40898594f, -0.046477184f, 0.18185264f),
      Float3(-0.2826749f, 0.07417482f, 0.34218854f),
      Float3(0.34838647f, 0.22544225f, -0.1740766f),
      Float3(-0.32264152f, -0.14205854f, -0.27968165f),
      Float3(0.4330735f, -0.11886856f, -0.028594075f),
      Float3(-0.08717822f, -0.39098963f, -0.20500502f),
      Float3(-0.21496783f, 0.3939974f, -0.032478984f),
      Float3(-0.26873308f, 0.32268628f, -0.16172849f),
      Float3(0.2105665f, -0.1961317f, -0.34596834f),
      Float3(0.43618459f, -0.11055175f, 0.0046166084f),
      Float3(0.053333335f, -0.3136395f, -0.31825432f),
      Float3(-0.059862167f, 0.13610291f, -0.4247264f),
      Float3(0.36649886f, 0.2550543f, -0.055909745f),
      Float3(-0.23410155f, -0.18240573f, 0.33826706f),
      Float3(-0.047309477f, -0.422215f, -0.14831145f),
      Float3(-0.23915662f, -0.25776964f, -0.28081828f),
      Float3(-0.1242081f, 0.42569533f, -0.07652336f),
      Float3(0.26148328f, -0.36501792f, 0.02980623f),
      Float3(-0.27287948f, -0.3499629f, 0.07458405f),
      Float3(0.0078929f, -0.16727713f, 0.41767937f),
      Float3(-0.017303303f, 0.29784867f, -0.33687797f),
      Float3(0.20548357f, -0.32526004f, -0.23341466f),
      Float3(-0.3231995f, 0.15642828f, -0.2712421f),
      Float3(-0.2669546f, 0.25993437f, -0.2523279f),
      Float3(-0.05554373f, 0.3170814f, -0.3144428f),
      Float3(-0.20839357f, -0.31092283f, -0.24979813f),
      Float3(0.06989323f, -0.31561416f, 0.31305373f),
      Float3(0.38475662f, -0.16053091f, -0.16938764f),
      Float3(-0.30262154f, -0.30015376f, -0.14431883f),
      Float3(0.34507355f, 0.0861152f, 0.27569625f),
      Float3(0.18144733f, -0.27887824f, -0.3029914f),
      Float3(-0.038550105f, 0.09795111f, 0.4375151f),
      Float3(0.35336703f, 0.26657528f, 0.08105161f),
      Float3(-0.007945601f, 0.14035943f, -0.42747644f),
      Float3(0.40630993f, -0.14917682f, -0.123119935f),
      Float3(-0.20167735f, 0.008816271f, -0.40217972f),
      Float3(-0.075270556f, -0.42564347f, -0.12514779f),
    )


  private fun fastFloor(f: Float): Int {
    return if (f >= 0) f.toInt() else f.toInt() - 1
  }


  private fun fastRound(f: Float): Int {
    return if (f >= 0) (f + 0.5f).toInt() else (f - 0.5f).toInt()
  }


  private fun lerp(a: Float, b: Float, t: Float): Float {
    return a + t * (b - a)
  }


  private fun interpHermiteFunc(t: Float): Float {
    return t * t * (3 - 2 * t)
  }


  private fun interpQuinticFunc(t: Float): Float {
    return t * t * t * (t * (t * 6 - 15) + 10)
  }


  private fun cubicLerp(a: Float, b: Float, c: Float, d: Float, t: Float): Float {
    val p = (d - c) - (a - b)
    return t * t * t * p + t * t * ((a - b) - p) + t * (c - a) + b
  }

  private fun calculateFractalBounding() {
    var amp = gain
    var ampFractal = 1f
    for (i in 1 until octaves) {
      ampFractal += amp
      amp *= gain
    }
    fractalBounding = 1f / ampFractal
  }

  // Hashing
  private val X_PRIME = 1619
  private val Y_PRIME = 31337
  private val Z_PRIME = 6971
  private val W_PRIME = 1013

  private fun hash2D(seed: Int, x: Int, y: Int): Int {
    var hash = seed
    hash = hash xor X_PRIME * x
    hash = hash xor Y_PRIME * y

    hash *= hash * hash * 60493
    hash = (hash shr 13) xor hash

    return hash
  }

  private fun hash3D(seed: Int, x: Int, y: Int, z: Int): Int {
    var hash = seed
    hash = hash xor X_PRIME * x
    hash = hash xor Y_PRIME * y
    hash = hash xor Z_PRIME * z

    hash *= hash * hash * 60493
    hash = (hash shr 13) xor hash

    return hash
  }

  private fun hash4D(seed: Int, x: Int, y: Int, z: Int, w: Int): Int {
    var hash = seed
    hash = hash xor X_PRIME * x
    hash = hash xor Y_PRIME * y
    hash = hash xor Z_PRIME * z
    hash = hash xor W_PRIME * w

    hash *= hash * hash * 60493
    hash = (hash shr 13) xor hash

    return hash
  }

  private fun valCoord2D(seed: Int, x: Int, y: Int): Float {
    var n = seed
    n = n xor X_PRIME * x
    n = n xor Y_PRIME * y

    return (n * n * n * 60493) / 2.14748365E9f
  }

  private fun valCoord3D(seed: Int, x: Int, y: Int, z: Int): Float {
    var n = seed
    n = n xor X_PRIME * x
    n = n xor Y_PRIME * y
    n = n xor Z_PRIME * z

    return (n * n * n * 60493) / 2.14748365E9f
  }

  private fun valCoord4D(seed: Int, x: Int, y: Int, z: Int, w: Int): Float {
    var n = seed
    n = n xor X_PRIME * x
    n = n xor Y_PRIME * y
    n = n xor Z_PRIME * z
    n = n xor W_PRIME * w

    return (n * n * n * 60493) / 2.14748365E9f
  }

  private fun gradCoord2D(seed: Int, x: Int, y: Int, xd: Float, yd: Float): Float {
    var hash = seed
    hash = hash xor X_PRIME * x
    hash = hash xor Y_PRIME * y

    hash *= hash * hash * 60493
    hash = (hash shr 13) xor hash

    val g = GRAD_2D[hash and 7]

    return xd * g.x + yd * g.y
  }

  private fun gradCoord3D(seed: Int, x: Int, y: Int, z: Int, xd: Float, yd: Float, zd: Float): Float {
    var hash = seed
    hash = hash xor X_PRIME * x
    hash = hash xor Y_PRIME * y
    hash = hash xor Z_PRIME * z

    hash *= hash * hash * 60493
    hash = (hash shr 13) xor hash

    val g = GRAD_3D[hash and 15]

    return xd * g.x + yd * g.y + zd * g.z
  }

  private fun gradCoord4D(seed: Int, x: Int, y: Int, z: Int, w: Int, xd: Float, yd: Float, zd: Float, wd: Float): Float {
    var hash = seed
    hash = hash xor X_PRIME * x
    hash = hash xor Y_PRIME * y
    hash = hash xor Z_PRIME * z
    hash = hash xor W_PRIME * w

    hash *= hash * hash * 60493
    hash = (hash shr 13) xor hash

    hash = hash and 31
    var a = yd
    var b = zd
    var c = wd            // X,Y,Z
    when (hash shr 3) {          // OR, DEPENDING ON HIGH ORDER 2 BITS:
      1 -> {
        a = wd
        b = xd
        c = yd
      }     // W,X,Y
      2 -> {
        a = zd
        b = wd
        c = xd
      }
      // Z,W,X
      3 -> {
        a = yd
        b = zd
        c = wd
        // Y,Z,W
      }
    }
    return (if ((hash and 4) == 0) -a else a) + (if ((hash and 2) == 0) -b else b) + (if ((hash and 1) == 0) -c else c)
  }

  fun getNoise(x1: Float, y1: Float, z1: Float): Float {
    val x = x1 * frequency
    val y = y1 * frequency
    val z = z1 * frequency

    when (noiseType) {
      Value -> return singleValue(seed, x, y, z)
      ValueFractal ->
        return when(fractalType) {
          FBM -> singleValueFractalFBM(x, y, z)
          Billow -> singleValueFractalBillow(x, y, z)
          RigidMulti -> singleValueFractalRigidMulti(x, y, z)
          else -> 0f
        }
      Perlin -> return singlePerlin(seed, x, y, z)
      PerlinFractal ->
        return when (fractalType) {
          FBM -> singlePerlinFractalFBM(x, y, z)
          Billow -> singlePerlinFractalBillow(x, y, z)
          RigidMulti -> singlePerlinFractalRigidMulti(x, y, z)
          else -> 0f
        }
      Simplex -> return singleSimplex(seed, x, y, z)
      SimplexFractal ->
        return when (fractalType) {
          FBM -> singleSimplexFractalFBM(x, y, z)
          Billow -> singleSimplexFractalBillow(x, y, z)
          RigidMulti -> singleSimplexFractalRigidMulti(x, y, z)
          else -> 0f
        }
      Cellular ->
        return when (cellularReturnType) {
          CellValue,NoiseLookup,Distance-> singleCellular(x, y, z)
          else -> singleCellular2Edge(x, y, z)
        }
      WhiteNoise -> return getWhiteNoise(x, y, z)
      Cubic -> return singleCubic(seed, x, y, z)
      CubicFractal ->
        return when (fractalType) {
          FBM -> singleCubicFractalFBM(x, y, z)
          Billow -> singleCubicFractalBillow(x, y, z)
          RigidMulti -> singleCubicFractalRigidMulti(x, y, z)
          else -> 0f
        }
      else -> return 0f
    }
  }

  fun getNoise(x1: Float, y1: Float): Float {
    val x = x1 * frequency
    val y = y1 * frequency

    when (noiseType) {
      Value -> return singleValue(seed, x, y)
      ValueFractal ->
        return when (fractalType) {
          FBM -> singleValueFractalFBM(x, y)
          Billow -> singleValueFractalBillow(x, y)
          RigidMulti -> singleValueFractalRigidMulti(x, y)
          else -> 0f
        }
      Perlin -> return singlePerlin(seed, x, y)
      PerlinFractal ->
        return when (fractalType) {
          FBM -> singlePerlinFractalFBM(x, y)
          Billow -> singlePerlinFractalBillow(x, y)
          RigidMulti -> singlePerlinFractalRigidMulti(x, y)
          else -> 0f
        }
      Simplex -> return singleSimplex(seed, x, y)
      SimplexFractal ->
        return when (fractalType) {
          FBM -> singleSimplexFractalFBM(x, y)
          Billow -> singleSimplexFractalBillow(x, y)
          RigidMulti -> singleSimplexFractalRigidMulti(x, y)
          else -> 0f
        }
      Cellular ->
        return when (cellularReturnType) {
          CellValue, NoiseLookup, Distance -> singleCellular(x, y)
          else -> singleCellular2Edge(x, y)
        }
      WhiteNoise -> return getWhiteNoise(x, y)
      Cubic ->return singleCubic(seed, x, y)
      CubicFractal ->
        return when (fractalType) {
          FBM -> singleCubicFractalFBM(x, y)
          Billow -> singleCubicFractalBillow(x, y)
          RigidMulti -> singleCubicFractalRigidMulti(x, y)
          else -> 0f
        }
      else -> return 0f
    }
  }

  // White Noise

  private fun floatCast2Int(f: Float): Int {
    val i = f.toRawBits()
    return i xor (i shr 16)
  }

  fun getWhiteNoise(x: Float, y: Float, z: Float, w: Float): Float {
    val xi = floatCast2Int(x)
    val yi = floatCast2Int(y)
    val zi = floatCast2Int(z)
    val wi = floatCast2Int(w)

    return valCoord4D(seed, xi, yi, zi, wi)
  }

  fun getWhiteNoise(x: Float, y: Float, z: Float): Float {
    val xi = floatCast2Int(x)
    val yi = floatCast2Int(y)
    val zi = floatCast2Int(z)

    return valCoord3D(seed, xi, yi, zi)
  }

  fun getWhiteNoise(x: Float, y: Float): Float {
    val xi = floatCast2Int(x)
    val yi = floatCast2Int(y)

    return valCoord2D(seed, xi, yi)
  }

  fun getWhiteNoiseInt(x: Int, y: Int, z: Int, w: Int): Float {
    return valCoord4D(seed, x, y, z, w)
  }

  fun getWhiteNoiseInt(x: Int, y: Int, z: Int): Float {
    return valCoord3D(seed, x, y, z)
  }

  fun getWhiteNoiseInt(x: Int, y: Int): Float {
    return valCoord2D(seed, x, y)
  }

  // Value Noise
  fun getValueFractal(x1: Float, y1: Float, z1: Float): Float {
    val x = x1 * frequency
    val y = y1 * frequency
    val z = z1 * frequency

    return when (fractalType) {
      FBM -> singleValueFractalFBM(x, y, z)
      Billow -> singleValueFractalBillow(x, y, z)
      RigidMulti -> singleValueFractalRigidMulti(x, y, z)
      else -> 0f
    }
  }

  fun singleValueFractalFBM(x1: Float, y1: Float, z1: Float): Float {

    var x = x1
    var y = y1
    var z = z1

    var seed = seed
    var sum = singleValue(seed, x, y, z)
    var amp = 1f

    for (i in 1 until octaves) {
      x *= lacunarity
      y *= lacunarity
      z *= lacunarity

      amp *= gain
      sum += singleValue(++seed, x, y, z) * amp
    }

    return sum * fractalBounding
  }

  fun singleValueFractalBillow(x1: Float, y1: Float, z1: Float): Float {

    var x = x1
    var y = y1
    var z = z1

    var seed = seed
    var sum = abs(singleValue(seed, x, y, z)) * 2 - 1
    var amp = 1f

    for (i in 1 until octaves) {
      x *= lacunarity
      y *= lacunarity
      z *= lacunarity

      amp *= gain
      sum += (abs(singleValue(++seed, x, y, z)) * 2 - 1) * amp
    }

    return sum * fractalBounding
  }

  fun singleValueFractalRigidMulti(x1: Float, y1: Float, z1: Float): Float {

    var x = x1
    var y = y1
    var z = z1

    var seed = seed
    var sum = 1 - abs(singleValue(seed, x, y, z))
    var amp = 1f

    for (i in 1 until octaves) {
      x *= lacunarity
      y *= lacunarity
      z *= lacunarity

      amp *= gain
      sum -= (1 - abs(singleValue(++seed, x, y, z))) * amp
    }

    return sum
  }

  fun getValue(x: Float, y: Float, z: Float): Float {
    return singleValue(seed, x * frequency, y * frequency, z * frequency)
  }

  fun singleValue(seed: Int, x: Float, y: Float, z: Float): Float {
    val x0 = fastFloor(x)
    val y0 = fastFloor(y)
    val z0 = fastFloor(z)
    val x1 = x0 + 1
    val y1 = y0 + 1
    val z1 = z0 + 1

    var xs = 0f
    var ys = 0f
    var zs = 0f

    when (interp) {
      Hermite -> {
        xs = interpHermiteFunc(x - x0)
        ys = interpHermiteFunc(y - y0)
        zs = interpHermiteFunc(z - z0)
      }
      Quintic -> {
        xs = interpQuinticFunc(x - x0)
        ys = interpQuinticFunc(y - y0)
        zs = interpQuinticFunc(z - z0)
      }
      else -> {
        xs = x - x0
        ys = y - y0
        zs = z - z0
      }
    }

    val xf00 = lerp(valCoord3D(seed, x0, y0, z0), valCoord3D(seed, x1, y0, z0), xs)
    val xf10 = lerp(valCoord3D(seed, x0, y1, z0), valCoord3D(seed, x1, y1, z0), xs)
    val xf01 = lerp(valCoord3D(seed, x0, y0, z1), valCoord3D(seed, x1, y0, z1), xs)
    val xf11 = lerp(valCoord3D(seed, x0, y1, z1), valCoord3D(seed, x1, y1, z1), xs)

    val yf0 = lerp(xf00, xf10, ys)
    val yf1 = lerp(xf01, xf11, ys)

    return lerp(yf0, yf1, zs)
  }

  fun getValueFractal(x1: Float, y1: Float): Float {

    var x = x1
    var y = y1

    x *= frequency
    y *= frequency

    return when (fractalType) {
      FBM -> singleValueFractalFBM(x, y)
      Billow -> singleValueFractalBillow(x, y)
      RigidMulti -> singleValueFractalRigidMulti(x, y)
      else -> 0f
    }
  }

  fun singleValueFractalFBM(x1: Float, y1: Float): Float {

    var x = x1
    var y = y1

    var seed = seed
    var sum = singleValue(seed, x, y)
    var amp = 1f

    for (i in 1 until octaves) {
      x *= lacunarity
      y *= lacunarity

      amp *= gain
      sum += singleValue(++seed, x, y) * amp
    }

    return sum * fractalBounding
  }

  fun singleValueFractalBillow(x1: Float, y1: Float): Float {

    var x = x1
    var y = y1

    var seed = seed
    var sum = abs(singleValue(seed, x, y)) * 2 - 1
    var amp = 1f

    for (i in 1 until octaves) {
      x *= lacunarity
      y *= lacunarity
      amp *= gain
      sum += (abs(singleValue(++seed, x, y)) * 2 - 1) * amp
    }

    return sum * fractalBounding
  }

  fun singleValueFractalRigidMulti(x1: Float, y1: Float): Float {

    var x = x1
    var y = y1

    var seed = seed
    var sum = 1 - abs(singleValue(seed, x, y))
    var amp = 1f

    for (i in 1 until octaves) {
      x *= lacunarity
      y *= lacunarity

      amp *= gain
      sum -= (1 - abs(singleValue(++seed, x, y))) * amp
    }

    return sum
  }

  fun getValue(x: Float, y: Float): Float {
    return singleValue(seed, x * frequency, y * frequency)
  }

  fun singleValue(seed: Int, x: Float, y: Float): Float {
    val x0 = fastFloor(x)
    val y0 = fastFloor(y)
    val x1 = x0 + 1
    val y1 = y0 + 1

    var xs = 0f
    var ys = 0f

    when (interp) {
      Hermite -> {
        xs = interpHermiteFunc(x - x0)
        ys = interpHermiteFunc(y - y0)
      }
      Quintic -> {
        xs = interpQuinticFunc(x - x0)
        ys = interpQuinticFunc(y - y0)
      }
      else -> {
        xs = x - x0
        ys = y - y0
      }
    }

    val xf0 = lerp(valCoord2D(seed, x0, y0), valCoord2D(seed, x1, y0), xs)
    val xf1 = lerp(valCoord2D(seed, x0, y1), valCoord2D(seed, x1, y1), xs)

    return lerp(xf0, xf1, ys)
  }

  // Gradient Noise
  fun getPerlinFractal(x1: Float, y1: Float, z1: Float): Float {

    var x = x1
    var y = y1
    var z = z1

    x *= frequency
    y *= frequency
    z *= frequency

    return when (fractalType) {
      FBM -> singlePerlinFractalFBM(x, y, z)
      Billow -> singlePerlinFractalBillow(x, y, z)
      RigidMulti -> singlePerlinFractalRigidMulti(x, y, z)
      else -> 0f
    }
  }

  fun singlePerlinFractalFBM(x1: Float, y1: Float, z1: Float): Float {

    var x = x1
    var y = y1
    var z = z1

    var seed = seed
    var sum = singlePerlin(seed, x, y, z)
    var amp = 1f

    for (i in 1 until octaves) {
      x *= lacunarity
      y *= lacunarity
      z *= lacunarity

      amp *= gain
      sum += singlePerlin(++seed, x, y, z) * amp
    }

    return sum * fractalBounding
  }

  fun singlePerlinFractalBillow(x1: Float, y1: Float, z1: Float): Float {

    var x = x1
    var y = y1
    var z = z1

    var seed = seed
    var sum = abs(singlePerlin(seed, x, y, z)) * 2 - 1
    var amp = 1f

    for (i in 1 until octaves) {
      x *= lacunarity
      y *= lacunarity
      z *= lacunarity

      amp *= gain
      sum += (abs(singlePerlin(++seed, x, y, z)) * 2 - 1) * amp
    }

    return sum * fractalBounding
  }

  fun singlePerlinFractalRigidMulti(x1: Float, y1: Float, z1: Float): Float {

    var x = x1
    var y = y1
    var z = z1

    var seed = seed
    var sum = 1 - abs(singlePerlin(seed, x, y, z))
    var amp = 1f

    for (i in 1 until octaves) {
      x *= lacunarity
      y *= lacunarity
      z *= lacunarity

      amp *= gain
      sum -= (1 - abs(singlePerlin(++seed, x, y, z))) * amp
    }

    return sum
  }

  fun getPerlin(x: Float, y: Float, z: Float): Float {
    return singlePerlin(seed, x * frequency, y * frequency, z * frequency)
  }

  fun singlePerlin(seed: Int, x: Float, y: Float, z: Float): Float {

    val x0 = fastFloor(x)
    val y0 = fastFloor(y)
    val z0 = fastFloor(z)
    val x1 = x0 + 1
    val y1 = y0 + 1
    val z1 = z0 + 1

    var xs = 0f
    var ys = 0f
    var zs = 0f
    when (interp) {
      Hermite -> {
        xs = interpHermiteFunc(x - x0)
        ys = interpHermiteFunc(y - y0)
        zs = interpHermiteFunc(z - z0)
      }
      Quintic -> {
        xs = interpQuinticFunc(x - x0)
        ys = interpQuinticFunc(y - y0)
        zs = interpQuinticFunc(z - z0)
      }
      else -> {
        xs = x - x0
        ys = y - y0
        zs = z - z0
      }
    }

    val xd0 = x - x0
    val yd0 = y - y0
    val zd0 = z - z0
    val xd1 = xd0 - 1
    val yd1 = yd0 - 1
    val zd1 = zd0 - 1

    val xf00 =
      lerp(gradCoord3D(seed, x0, y0, z0, xd0, yd0, zd0), gradCoord3D(seed, x1, y0, z0, xd1, yd0, zd0), xs)
    val xf10 =
      lerp(gradCoord3D(seed, x0, y1, z0, xd0, yd1, zd0), gradCoord3D(seed, x1, y1, z0, xd1, yd1, zd0), xs)
    val xf01 =
      lerp(gradCoord3D(seed, x0, y0, z1, xd0, yd0, zd1), gradCoord3D(seed, x1, y0, z1, xd1, yd0, zd1), xs)
    val xf11 =
      lerp(gradCoord3D(seed, x0, y1, z1, xd0, yd1, zd1), gradCoord3D(seed, x1, y1, z1, xd1, yd1, zd1), xs)

    val yf0 = lerp(xf00, xf10, ys)
    val yf1 = lerp(xf01, xf11, ys)

    return lerp(yf0, yf1, zs)
  }

  fun getPerlinFractal(x1: Float, y1: Float): Float {

    var x = x1
    var y = y1

    x *= frequency
    y *= frequency

    return when (fractalType) {
      FBM -> singlePerlinFractalFBM(x, y)
      Billow -> singlePerlinFractalBillow(x, y)
      RigidMulti -> singlePerlinFractalRigidMulti(x, y)
      else -> 0f
    }
  }

  fun singlePerlinFractalFBM(x1: Float, y1: Float): Float {

    var x = x1
    var y = y1

    var seed = seed
    var sum = singlePerlin(seed, x, y)
    var amp = 1f

    for (i in 1 until octaves) {
      x *= lacunarity
      y *= lacunarity

      amp *= gain
      sum += singlePerlin(++seed, x, y) * amp
    }

    return sum * fractalBounding
  }

  fun singlePerlinFractalBillow(x1: Float, y1: Float): Float {

    var x = x1
    var y = y1

    var seed = seed
    var sum = abs(singlePerlin(seed, x, y)) * 2 - 1
    var amp = 1f

    for (i in 1 until octaves) {
      x *= lacunarity
      y *= lacunarity

      amp *= gain
      sum += (abs(singlePerlin(++seed, x, y)) * 2 - 1) * amp
    }

    return sum * fractalBounding
  }

  fun singlePerlinFractalRigidMulti(x1: Float, y1: Float): Float {

    var x = x1
    var y = y1

    var seed = seed
    var sum = 1 - abs(singlePerlin(seed, x, y))
    var amp = 1f

    for (i in 1 until octaves) {
      x *= lacunarity
      y *= lacunarity

      amp *= gain
      sum -= (1 - abs(singlePerlin(++seed, x, y))) * amp
    }

    return sum
  }

  fun getPerlin(x: Float, y: Float): Float {
    return singlePerlin(seed, x * frequency, y * frequency)
  }

  fun singlePerlin(seed: Int, x: Float, y: Float): Float {
    val x0 = fastFloor(x)
    val y0 = fastFloor(y)
    val x1 = x0 + 1
    val y1 = y0 + 1

    var xs = 0f
    var ys = 0f
    when (interp) {
      Hermite -> {
        xs = interpHermiteFunc(x - x0)
        ys = interpHermiteFunc(y - y0)
      }
      Quintic -> {
        xs = interpQuinticFunc(x - x0)
        ys = interpQuinticFunc(y - y0)
      }
      else -> {
        xs = x - x0
        ys = y - y0
      }
    }

    val xd0 = x - x0
    val yd0 = y - y0
    val xd1 = xd0 - 1
    val yd1 = yd0 - 1

    val xf0 = lerp(gradCoord2D(seed, x0, y0, xd0, yd0), gradCoord2D(seed, x1, y0, xd1, yd0), xs)
    val xf1 = lerp(gradCoord2D(seed, x0, y1, xd0, yd1), gradCoord2D(seed, x1, y1, xd1, yd1), xs)

    return lerp(xf0, xf1, ys)
  }

  // Simplex Noise
  fun getSimplexFractal(x1: Float, y1: Float, z1: Float): Float {

    var x = x1
    var y = y1
    var z = z1

    x *= frequency
    y *= frequency
    z *= frequency

    return when (fractalType) {
      FBM -> singleSimplexFractalFBM(x, y, z)
      Billow -> singleSimplexFractalBillow(x, y, z)
      RigidMulti -> singleSimplexFractalRigidMulti(x, y, z)
      else -> 0f
    }
  }

  fun singleSimplexFractalFBM(x1: Float, y1: Float, z1: Float): Float {

    var x = x1
    var y = y1
    var z = z1

    var seed = seed
    var sum = singleSimplex(seed, x, y, z)
    var amp = 1f

    for (i in 1 until octaves) {
      x *= lacunarity
      y *= lacunarity
      z *= lacunarity

      amp *= gain
      sum += singleSimplex(++seed, x, y, z) * amp
    }

    return sum * fractalBounding
  }

  fun singleSimplexFractalBillow(x1: Float, y1: Float, z1: Float): Float {

    var x = x1
    var y = y1
    var z = z1

    var seed = seed
    var sum = abs(singleSimplex(seed, x, y, z)) * 2 - 1
    var amp = 1f

    for (i in 1 until octaves) {
      x *= lacunarity
      y *= lacunarity
      z *= lacunarity

      amp *= gain
      sum += (abs(singleSimplex(++seed, x, y, z)) * 2 - 1) * amp
    }

    return sum * fractalBounding
  }

  fun singleSimplexFractalRigidMulti(x1: Float, y1: Float, z1: Float): Float {

    var x = x1
    var y = y1
    var z = z1

    var seed = seed
    var sum = 1 - abs(singleSimplex(seed, x, y, z))
    var amp = 1f

    for (i in 1 until octaves) {
      x *= lacunarity
      y *= lacunarity
      z *= lacunarity

      amp *= gain
      sum -= (1 - abs(singleSimplex(++seed, x, y, z))) * amp
    }

    return sum
  }

  fun getSimplex(x: Float, y: Float, z: Float): Float {
    return singleSimplex(seed, x * frequency, y * frequency, z * frequency)
  }

  private val F3 = (1.0f / 3.0f)
  private val G3 = (1.0f / 6.0f)
  private val G33 = G3 * 3f - 1f

  fun singleSimplex(seed: Int, x: Float, y: Float, z: Float): Float {
    var t = (x + y + z) * F3
    val i: Int = fastFloor(x + t)
    val j: Int = fastFloor(y + t)
    val k: Int = fastFloor(z + t)

    t = (i + j + k) * G3
    val x0 = x - (i - t)
    val y0 = y - (j - t)
    val z0 = z - (k - t)

    var i1: Int = 0
    var j1: Int = 0
    var k1: Int = 0
    var i2: Int = 0
    var j2: Int = 0
    var k2: Int = 0

    if (x0 >= y0) {
      if (y0 >= z0) {
        i1 = 1
        j1 = 0
        k1 = 0
        i2 = 1
        j2 = 1
        k2 = 0
      } else if (x0 >= z0) {
        i1 = 1
        j1 = 0
        k1 = 0
        i2 = 1
        j2 = 0
        k2 = 1
      } else // x0 < z0
      {
        i1 = 0
        j1 = 0
        k1 = 1
        i2 = 1
        j2 = 0
        k2 = 1
      }
    } else // x0 < y0
    {
      if (y0 < z0) {
        i1 = 0
        j1 = 0
        k1 = 1
        i2 = 0
        j2 = 1
        k2 = 1
      } else if (x0 < z0) {
        i1 = 0
        j1 = 1
        k1 = 0
        i2 = 0
        j2 = 1
        k2 = 1
      } else // x0 >= z0
      {
        i1 = 0
        j1 = 1
        k1 = 0
        i2 = 1
        j2 = 1
        k2 = 0
      }
    }

    val x1 = x0 - i1 + G3
    val y1 = y0 - j1 + G3
    val z1 = z0 - k1 + G3
    val x2 = x0 - i2 + F3
    val y2 = y0 - j2 + F3
    val z2 = z0 - k2 + F3
    val x3 = x0 + G33
    val y3 = y0 + G33
    val z3 = z0 + G33

    var n0 = 0f
    var n1 = 0f
    var n2 = 0f
    var n3 = 0f

    t = 0.6f - x0 * x0 - y0 * y0 - z0 * z0
    if (t < 0) n0 = 0f
    else {
      t *= t
      n0 = t * t * gradCoord3D(seed, i, j, k, x0, y0, z0)
    }

    t = 0.6f - x1 * x1 - y1 * y1 - z1 * z1
    if (t < 0) n1 = 0f
    else {
      t *= t
      n1 = t * t * gradCoord3D(seed, i + i1, j + j1, k + k1, x1, y1, z1)
    }

    t = 0.6f - x2 * x2 - y2 * y2 - z2 * z2
    if (t < 0) n2 = 0f
    else {
      t *= t
      n2 = t * t * gradCoord3D(seed, i + i2, j + j2, k + k2, x2, y2, z2)
    }

    t = 0.6f - x3 * x3 - y3 * y3 - z3 * z3
    if (t < 0) n3 = 0f
    else {
      t *= t
      n3 = t * t * gradCoord3D(seed, i + 1, j + 1, k + 1, x3, y3, z3)
    }

    return 32 * (n0 + n1 + n2 + n3)
  }

  fun getSimplexFractal(x1: Float, y1: Float): Float {

    var x = x1
    var y = y1

    x *= frequency
    y *= frequency

    return when (fractalType) {
      FBM -> singleSimplexFractalFBM(x, y)
      Billow -> singleSimplexFractalBillow(x, y)
      RigidMulti -> singleSimplexFractalRigidMulti(x, y)
      else -> 0f
    }
  }

  fun singleSimplexFractalFBM(x1: Float, y1: Float): Float {

    var x = x1
    var y = y1

    var seed = seed
    var sum = singleSimplex(seed, x, y)
    var amp = 1f

    for (i in 1 until octaves) {
      x *= lacunarity
      y *= lacunarity

      amp *= gain
      sum += singleSimplex(++seed, x, y) * amp
    }

    return sum * fractalBounding
  }

  fun singleSimplexFractalBillow(x1: Float, y1: Float): Float {

    var x = x1
    var y = y1

    var seed = seed
    var sum = abs(singleSimplex(seed, x, y)) * 2 - 1
    var amp = 1f

    for (i in 1 until octaves) {
      x *= lacunarity
      y *= lacunarity

      amp *= gain
      sum += (abs(singleSimplex(++seed, x, y)) * 2 - 1) * amp
    }

    return sum * fractalBounding
  }

  fun singleSimplexFractalRigidMulti(x1: Float, y1: Float): Float {

    var x = x1
    var y = y1

    var seed = seed
    var sum = 1 - abs(singleSimplex(seed, x, y))
    var amp = 1f

    for (i in 1 until octaves) {
      x *= lacunarity
      y *= lacunarity

      amp *= gain
      sum -= (1 - abs(singleSimplex(++seed, x, y))) * amp
    }

    return sum
  }

  fun getSimplex(x: Float, y: Float): Float {
    return singleSimplex(seed, x * frequency, y * frequency)
  }

  private val SQRT3 = 1.7320508f
  private val F2 = 0.5f * (SQRT3 - 1.0f)
  private val G2 = (3.0f - SQRT3) / 6.0f

  fun singleSimplex(seed: Int, x: Float, y: Float): Float {
    var t = (x + y) * F2
    val i = fastFloor(x + t)
    val j = fastFloor(y + t)

    t = (i + j) * G2
    val X0 = i - t
    val Y0 = j - t

    val x0 = x - X0
    val y0 = y - Y0

    var i1 = 0
    var j1 = 0

    if (x0 > y0) {
      i1 = 1
      j1 = 0
    } else {
      i1 = 0
      j1 = 1
    }

    val x1 = x0 - i1 + G2
    val y1 = y0 - j1 + G2
    val x2 = x0 - 1 + 2*G2
    val y2 = y0 - 1 + 2*G2

    var n0 = 0f
    var n1 = 0f
    var n2 = 0f

    t = 0.5f - x0 * x0 - y0 * y0
    if (t < 0) n0 = 0f
    else {
      t *= t
      n0 = t * t * gradCoord2D(seed, i, j, x0, y0)
    }

    t = 0.5f - x1 * x1 - y1 * y1
    if (t < 0) n1 = 0f
    else {
      t *= t
      n1 = t * t * gradCoord2D(seed, i + i1, j + j1, x1, y1)
    }

    t = 0.5f - x2 * x2 - y2 * y2
    if (t < 0) n2 = 0f
    else {
      t *= t
      n2 = t * t * gradCoord2D(seed, i + 1, j + 1, x2, y2)
    }

    return 50 * (n0 + n1 + n2)
  }

  fun getSimplex(x: Float, y: Float, z: Float, w: Float): Float {
    return singleSimplex(seed, x * frequency, y * frequency, z * frequency, w * frequency)
  }

  private val SIMPLEX_4D = byteArrayOf(
    0, 1, 2, 3, 0, 1, 3, 2, 0, 0, 0, 0, 0, 2, 3, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 3, 0,
    0, 2, 1, 3, 0, 0, 0, 0, 0, 3, 1, 2, 0, 3, 2, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 3, 2, 0,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    1, 2, 0, 3, 0, 0, 0, 0, 1, 3, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 3, 0, 1, 2, 3, 1, 0,
    1, 0, 2, 3, 1, 0, 3, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 3, 1, 0, 0, 0, 0, 2, 1, 3, 0,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    2, 0, 1, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 1, 2, 3, 0, 2, 1, 0, 0, 0, 0, 3, 1, 2, 0,
    2, 1, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 1, 0, 2, 0, 0, 0, 0, 3, 2, 0, 1, 3, 2, 1, 0
  )

  private val F4 = ((2.23606797f - 1.0f) / 4.0f)
  private val G4 = ((5.0f - 2.23606797f) / 20.0f)

  fun singleSimplex(seed: Int, x: Float, y: Float, z: Float, w: Float): Float {
    var n0 = 0f
    var n1 = 0f
    var n2 = 0f
    var n3 = 0f
    var n4 = 0f

    var t = (x + y + z + w) * F4
    val i = fastFloor(x + t)
    val j = fastFloor(y + t)
    val k = fastFloor(z + t)
    val l = fastFloor(w + t)
    t = (i + j + k + l) * G4
    val X0 = i - t
    val Y0 = j - t
    val Z0 = k - t
    val W0 = l - t
    val x0 = x - X0
    val y0 = y - Y0
    val z0 = z - Z0
    val w0 = w - W0

    var c = if (x0 > y0) 32 else 0
    c += if (x0 > z0) 16 else 0
    c += if (y0 > z0) 8 else 0
    c += if (x0 > w0) 4 else 0
    c += if (y0 > w0) 2 else 0
    c += if (z0 > w0) 1 else 0
    c = c shl 2

    val i1 = if (SIMPLEX_4D[c] >= 3) 1 else 0
    val i2 = if (SIMPLEX_4D[c] >= 2) 1 else 0
    val i3 = if (SIMPLEX_4D[c++] >= 1) 1 else 0
    val j1 = if (SIMPLEX_4D[c] >= 3) 1 else 0
    val j2 = if (SIMPLEX_4D[c] >= 2) 1 else 0
    val j3 = if (SIMPLEX_4D[c++] >= 1) 1 else 0
    val k1 = if (SIMPLEX_4D[c] >= 3) 1 else 0
    val k2 = if (SIMPLEX_4D[c] >= 2) 1 else 0
    val k3 = if (SIMPLEX_4D[c++] >= 1) 1 else 0
    val l1 = if (SIMPLEX_4D[c] >= 3) 1 else 0
    val l2 = if (SIMPLEX_4D[c] >= 2) 1 else 0
    val l3 = if (SIMPLEX_4D[c] >= 1) 1 else 0

    val x1 = x0 - i1 + G4
    val y1 = y0 - j1 + G4
    val z1 = z0 - k1 + G4
    val w1 = w0 - l1 + G4
    val x2 = x0 - i2 + 2 * G4
    val y2 = y0 - j2 + 2 * G4
    val z2 = z0 - k2 + 2 * G4
    val w2 = w0 - l2 + 2 * G4
    val x3 = x0 - i3 + 3 * G4
    val y3 = y0 - j3 + 3 * G4
    val z3 = z0 - k3 + 3 * G4
    val w3 = w0 - l3 + 3 * G4
    val x4 = x0 - 1 + 4 * G4
    val y4 = y0 - 1 + 4 * G4
    val z4 = z0 - 1 + 4 * G4
    val w4 = w0 - 1 + 4 * G4

    t = 0.6f - x0 * x0 - y0 * y0 - z0 * z0 - w0 * w0
    if (t < 0) n0 = 0f
    else {
      t *= t
      n0 = t * t * gradCoord4D(seed, i, j, k, l, x0, y0, z0, w0)
    }
    t = 0.6f - x1 * x1 - y1 * y1 - z1 * z1 - w1 * w1
    if (t < 0) n1 = 0f
    else {
      t *= t
      n1 = t * t * gradCoord4D(seed, i + i1, j + j1, k + k1, l + l1, x1, y1, z1, w1)
    }
    t = 0.6f - x2 * x2 - y2 * y2 - z2 * z2 - w2 * w2
    if (t < 0) n2 = 0f
    else {
      t *= t
      n2 = t * t * gradCoord4D(seed, i + i2, j + j2, k + k2, l + l2, x2, y2, z2, w2)
    }
    t = 0.6f - x3 * x3 - y3 * y3 - z3 * z3 - w3 * w3
    if (t < 0) n3 = 0f
    else {
      t *= t
      n3 = t * t * gradCoord4D(seed, i + i3, j + j3, k + k3, l + l3, x3, y3, z3, w3)
    }
    t = 0.6f - x4 * x4 - y4 * y4 - z4 * z4 - w4 * w4
    if (t < 0) n4 = 0f
    else {
      t *= t
      n4 = t * t * gradCoord4D(seed, i + 1, j + 1, k + 1, l + 1, x4, y4, z4, w4)
    }

    return 27 * (n0 + n1 + n2 + n3 + n4)
  }

  // Cubic Noise
  fun getCubicFractal(x1: Float, y1: Float, z1: Float): Float {

    var x = x1
    var y = y1
    var z = z1

    x *= frequency
    y *= frequency
    z *= frequency

    return when (fractalType) {
      FBM -> singleCubicFractalFBM(x, y, z)
      Billow -> singleCubicFractalBillow(x, y, z)
      RigidMulti -> singleCubicFractalRigidMulti(x, y, z)
      else -> 0f
    }
  }

  fun singleCubicFractalFBM(x1: Float, y1: Float, z1: Float): Float {

    var x = x1
    var y = y1
    var z = z1

    var seed = seed
    var sum = singleCubic(seed, x, y, z)
    var amp = 1f
    var i = 0

    while (++i < octaves) {
      x *= lacunarity
      y *= lacunarity
      z *= lacunarity

      amp *= gain
      sum += singleCubic(++seed, x, y, z) * amp
    }

    return sum * fractalBounding
  }

  fun singleCubicFractalBillow(x1: Float, y1: Float, z1: Float): Float {

    var x = x1
    var y = y1
    var z = z1

    var seed = seed
    var sum = abs(singleCubic(seed, x, y, z)) * 2 - 1
    var amp = 1f
    var i = 0

    while (++i < octaves) {
      x *= lacunarity
      y *= lacunarity
      z *= lacunarity

      amp *= gain
      sum += (abs(singleCubic(++seed, x, y, z)) * 2 - 1) * amp
    }

    return sum * fractalBounding
  }

  fun singleCubicFractalRigidMulti(x1: Float, y1: Float, z1: Float): Float {

    var x = x1
    var y = y1
    var z = z1

    var seed = seed
    var sum = 1 - abs(singleCubic(seed, x, y, z))
    var amp = 1f
    var i = 0

    while (++i < octaves) {
      x *= lacunarity
      y *= lacunarity
      z *= lacunarity

      amp *= gain
      sum -= (1 - abs(singleCubic(++seed, x, y, z))) * amp
    }

    return sum
  }

  fun getCubic(x: Float, y: Float, z: Float): Float {
    return singleCubic(seed, x * frequency, y * frequency, z * frequency)
  }

  private val CUBIC_3D_BOUNDING = 1f / (1.5f * 1.5f * 1.5f)

  fun singleCubic(seed: Int, x: Float, y: Float, z: Float): Float {
    val x1 = fastFloor(x)
    val y1 = fastFloor(y)
    val z1 = fastFloor(z)

    val x0 = x1 - 1
    val y0 = y1 - 1
    val z0 = z1 - 1
    val x2 = x1 + 1
    val y2 = y1 + 1
    val z2 = z1 + 1
    val x3 = x1 + 2
    val y3 = y1 + 2
    val z3 = z1 + 2

    val xs = x - x1.toFloat()
    val ys = y - y1.toFloat()
    val zs = z - z1.toFloat()

    return cubicLerp(
      cubicLerp(
        cubicLerp(valCoord3D(seed, x0, y0, z0), valCoord3D(seed, x1, y0, z0), valCoord3D(seed, x2, y0, z0), valCoord3D(seed, x3, y0, z0), xs),
        cubicLerp(valCoord3D(seed, x0, y1, z0), valCoord3D(seed, x1, y1, z0), valCoord3D(seed, x2, y1, z0), valCoord3D(seed, x3, y1, z0), xs),
        cubicLerp(valCoord3D(seed, x0, y2, z0), valCoord3D(seed, x1, y2, z0), valCoord3D(seed, x2, y2, z0), valCoord3D(seed, x3, y2, z0), xs),
        cubicLerp(valCoord3D(seed, x0, y3, z0), valCoord3D(seed, x1, y3, z0), valCoord3D(seed, x2, y3, z0), valCoord3D(seed, x3, y3, z0), xs),
        ys),
      cubicLerp(
        cubicLerp(valCoord3D(seed, x0, y0, z1), valCoord3D(seed, x1, y0, z1), valCoord3D(seed, x2, y0, z1), valCoord3D(seed, x3, y0, z1), xs),
        cubicLerp(valCoord3D(seed, x0, y1, z1), valCoord3D(seed, x1, y1, z1), valCoord3D(seed, x2, y1, z1), valCoord3D(seed, x3, y1, z1), xs),
        cubicLerp(valCoord3D(seed, x0, y2, z1), valCoord3D(seed, x1, y2, z1), valCoord3D(seed, x2, y2, z1), valCoord3D(seed, x3, y2, z1), xs),
        cubicLerp(valCoord3D(seed, x0, y3, z1), valCoord3D(seed, x1, y3, z1), valCoord3D(seed, x2, y3, z1), valCoord3D(seed, x3, y3, z1), xs),
        ys),
      cubicLerp(
        cubicLerp(valCoord3D(seed, x0, y0, z2), valCoord3D(seed, x1, y0, z2), valCoord3D(seed, x2, y0, z2), valCoord3D(seed, x3, y0, z2), xs),
        cubicLerp(valCoord3D(seed, x0, y1, z2), valCoord3D(seed, x1, y1, z2), valCoord3D(seed, x2, y1, z2), valCoord3D(seed, x3, y1, z2), xs),
        cubicLerp(valCoord3D(seed, x0, y2, z2), valCoord3D(seed, x1, y2, z2), valCoord3D(seed, x2, y2, z2), valCoord3D(seed, x3, y2, z2), xs),
        cubicLerp(valCoord3D(seed, x0, y3, z2), valCoord3D(seed, x1, y3, z2), valCoord3D(seed, x2, y3, z2), valCoord3D(seed, x3, y3, z2), xs),
        ys),
      cubicLerp(
        cubicLerp(valCoord3D(seed, x0, y0, z3), valCoord3D(seed, x1, y0, z3), valCoord3D(seed, x2, y0, z3), valCoord3D(seed, x3, y0, z3), xs),
        cubicLerp(valCoord3D(seed, x0, y1, z3), valCoord3D(seed, x1, y1, z3), valCoord3D(seed, x2, y1, z3), valCoord3D(seed, x3, y1, z3), xs),
        cubicLerp(valCoord3D(seed, x0, y2, z3), valCoord3D(seed, x1, y2, z3), valCoord3D(seed, x2, y2, z3), valCoord3D(seed, x3, y2, z3), xs),
        cubicLerp(valCoord3D(seed, x0, y3, z3), valCoord3D(seed, x1, y3, z3), valCoord3D(seed, x2, y3, z3), valCoord3D(seed, x3, y3, z3), xs),
        ys),
      zs) * CUBIC_3D_BOUNDING
  }


  fun getCubicFractal(x1: Float, y1: Float): Float {

    var x = x1
    var y = y1

    x *= frequency
    y *= frequency

    return when (fractalType) {
      FBM -> singleCubicFractalFBM(x, y)
      Billow -> singleCubicFractalBillow(x, y)
      RigidMulti -> singleCubicFractalRigidMulti(x, y)
      else -> 0f
    }
  }

  fun singleCubicFractalFBM(x1: Float, y1: Float): Float {

    var x = x1
    var y = y1

    var seed = seed
    var sum = singleCubic(seed, x, y)
    var amp = 1f
    var i = 0

    while (++i < octaves) {
      x *= lacunarity
      y *= lacunarity

      amp *= gain
      sum += singleCubic(++seed, x, y) * amp
    }

    return sum * fractalBounding
  }

  fun singleCubicFractalBillow(x1: Float, y1: Float): Float {

    var x = x1
    var y = y1

    var seed = seed
    var sum = abs(singleCubic(seed, x, y)) * 2 - 1
    var amp = 1f
    var i = 0

    while (++i < octaves) {
      x *= lacunarity
      y *= lacunarity

      amp *= gain
      sum += (abs(singleCubic(++seed, x, y)) * 2 - 1) * amp
    }

    return sum * fractalBounding
  }

  fun singleCubicFractalRigidMulti(x1: Float, y1: Float): Float {

    var x = x1
    var y = y1

    var seed = seed
    var sum = 1 - abs(singleCubic(seed, x, y))
    var amp = 1f
    var i = 0

    while (++i < octaves) {
      x *= lacunarity
      y *= lacunarity

      amp *= gain
      sum -= (1 - abs(singleCubic(++seed, x, y))) * amp
    }

    return sum
  }

  fun getCubic(x1: Float, y1: Float): Float {

    var x = x1
    var y = y1

    x *= frequency
    y *= frequency

    return singleCubic(0, x, y)
  }

  private val CUBIC_2D_BOUNDING = 1f / (1.5f * 1.5f)

  private fun singleCubic(seed: Int, x: Float, y: Float): Float {
    val x1 = fastFloor(x)
    val y1 = fastFloor(y)

    val x0 = x1 - 1
    val y0 = y1 - 1
    val x2 = x1 + 1
    val y2 = y1 + 1
    val x3 = x1 + 2
    val y3 = y1 + 2

    val xs = x - x1.toFloat()
    val ys = y - y1.toFloat()

    return cubicLerp(
      cubicLerp(valCoord2D(seed, x0, y0), valCoord2D(seed, x1, y0), valCoord2D(seed, x2, y0), valCoord2D(seed, x3, y0),
        xs),
      cubicLerp(valCoord2D(seed, x0, y1), valCoord2D(seed, x1, y1), valCoord2D(seed, x2, y1), valCoord2D(seed, x3, y1),
        xs),
      cubicLerp(valCoord2D(seed, x0, y2), valCoord2D(seed, x1, y2), valCoord2D(seed, x2, y2), valCoord2D(seed, x3, y2),
        xs),
      cubicLerp(valCoord2D(seed, x0, y3), valCoord2D(seed, x1, y3), valCoord2D(seed, x2, y3), valCoord2D(seed, x3, y3),
        xs),
      ys) * CUBIC_2D_BOUNDING
  }

  // Cellular Noise
  fun getCellular(x1: Float, y1: Float, z1: Float): Float {

    var x = x1
    var y = y1
    var z = z1

    x *= frequency
    y *= frequency
    z *= frequency

    return when (cellularReturnType) {
      CellValue,NoiseLookup,Distance -> singleCellular(x, y, z)
      else -> singleCellular2Edge(x, y, z)
    }
  }

  fun singleCellular(x: Float, y: Float, z: Float): Float {
    val xr = fastRound(x)
    val yr = fastRound(y)
    val zr = fastRound(z)

    var distance = 999999f
    var xc = 0
    var yc = 0
    var zc = 0

    when (cellularDistanceFunction) {
      Euclidean -> {
        for (xi in (xr - 1) .. (xr + 1)) {
          for (yi in (yr - 1) ..  (yr + 1)) {
            for (zi in (zr - 1) .. (zr + 1)) {
              val vec = CELL_3D [hash3D(seed, xi, yi, zi) and 255]

              val vecX = xi -x + vec.x
              val vecY = yi -y + vec.y
              val vecZ = zi -z + vec.z

              val newDistance = vecX * vecX + vecY * vecY + vecZ * vecZ

              if (newDistance < distance) {
                distance = newDistance
                xc = xi
                yc = yi
                zc = zi
              }
            }
          }
        }
      }
      Manhattan -> {
        for (xi in (xr - 1) .. (xr + 1)) {
          for (yi in (yr - 1) .. (yr + 1)) {
            for (zi in (zr - 1) .. (zr + 1)) {
              val vec = CELL_3D [hash3D(seed, xi, yi, zi) and 255]

              val vecX = xi -x + vec.x
              val vecY = yi -y + vec.y
              val vecZ = zi -z + vec.z

              val newDistance = abs (vecX) + abs(vecY) + abs(vecZ)

              if (newDistance < distance) {
                distance = newDistance
                xc = xi
                yc = yi
                zc = zi
              }
            }
          }
        }
      }
      Natural -> {
        for (xi in (xr - 1) .. (xr + 1)) {
          for (yi in (yr - 1) .. (yr + 1)) {
            for (zi in (zr - 1) .. (zr + 1)) {
              val vec = CELL_3D [hash3D(seed, xi, yi, zi) and 255]

              val vecX = xi -x + vec.x
              val vecY = yi -y + vec.y
              val vecZ = zi -z + vec.z

              val newDistance =(abs(vecX) + abs(vecY) + abs(vecZ)) + (vecX * vecX + vecY * vecY + vecZ * vecZ)

              if (newDistance < distance) {
                distance = newDistance
                xc = xi
                yc = yi
                zc = zi
              }
            }
          }
        }
      }
      else -> {}
    }

    return when (cellularReturnType) {
      CellValue -> valCoord3D(0, xc, yc, zc)
      NoiseLookup -> {
        val vec = CELL_3D [hash3D(seed, xc, yc, zc) and 255]
        if (cellularNoiseLookup == null) {
          throw RuntimeException("noise: Forgot to set m_cellularNoiseLookup.")
        }
        cellularNoiseLookup!!.getNoise(xc + vec.x, yc + vec.y, zc + vec.z)
      }

      Distance -> distance - 1
      else -> 0f
    }
  }

  fun singleCellular2Edge(x: Float, y: Float, z: Float): Float {
    val xr: Int = fastRound(x)
    val yr: Int = fastRound(y)
    val zr: Int = fastRound(z)

    var distance = 999999f
    var distance2 = 999999f

    when (cellularDistanceFunction) {
      Euclidean -> {
        for (xi in (xr - 1) .. (xr + 1)) {
          for (yi in (yr - 1) .. (yr + 1)) {
            for (zi in (zr - 1) .. (zr + 1)) {
              val vec = CELL_3D [hash3D(seed, xi, yi, zi) and 255]

              val vecX = xi -x + vec.x
              val vecY = yi -y + vec.y
              val vecZ = zi -z + vec.z

              val newDistance = vecX * vecX + vecY * vecY + vecZ * vecZ

              distance2 = max(min(distance2, newDistance), distance)
              distance = min(distance, newDistance)
            }
          }
        }
      }
      Manhattan -> {
        for (xi in (xr - 1) .. (xr + 1)) {
          for (yi in (yr - 1) .. (yr + 1)) {
            for (zi in (zr - 1) .. (zr + 1)) {
              val vec = CELL_3D [hash3D(seed, xi, yi, zi) and 255]

              val vecX = xi -x + vec.x
              val vecY = yi -y + vec.y
              val vecZ = zi -z + vec.z

              val newDistance = abs (vecX) + abs(vecY) + abs(vecZ)

              distance2 = max(min(distance2, newDistance), distance)
              distance = min(distance, newDistance)
            }
          }
        }
      }
      Natural -> {
        for (xi in (xr - 1) .. (xr + 1)) {
          for (yi in (yr - 1) .. (yr + 1)) {
            for (zi in (zr - 1) .. (zr + 1)) {
              val vec = CELL_3D [hash3D(seed, xi, yi, zi) and 255]

              val vecX = xi -x + vec.x
              val vecY = yi -y + vec.y
              val vecZ = zi -z + vec.z

              val newDistance =(abs(vecX) + abs(vecY) + abs(vecZ)) + (vecX * vecX + vecY * vecY + vecZ * vecZ)

              distance2 = max(min(distance2, newDistance), distance)
              distance = min(distance, newDistance)
            }
          }
        }
      }
      else -> {}
    }

    return when (cellularReturnType) {
      Distance2 -> distance2 - 1
      Distance2Add -> distance2 + distance - 1
      Distance2Sub -> distance2 - distance - 1
      Distance2Mul -> distance2 * distance - 1
      Distance2Div -> distance / distance2 - 1
      else -> 0f
    }
  }

  fun getCellular(x1: Float, y1: Float): Float {

    var x = x1
    var y = y1

    x *= frequency
    y *= frequency

    return when (cellularReturnType) {
      CellValue,NoiseLookup,Distance -> singleCellular(x, y)
      else -> singleCellular2Edge(x, y)
    }
  }

  fun singleCellular(x: Float, y: Float): Float {
    val xr: Int = fastRound(x)
    val yr: Int = fastRound(y)

    var distance = 999999f
    var xc: Int = 0
    var yc: Int = 0

    when (cellularDistanceFunction) {
      Manhattan -> {
        for (xi in (xr - 1) .. (xr + 1)) {
          for (yi in (yr - 1) .. (yr + 1)) {
            val vec = CELL_2D [hash2D(seed, xi, yi) and 255]

            val vecX = xi -x + vec.x
            val vecY = yi -y + vec.y

            val newDistance =(abs(vecX) + abs(vecY))

            if (newDistance < distance) {
              distance = newDistance
              xc = xi
              yc = yi
            }
          }
        }
      }
      Natural -> {
        for (xi in (xr - 1) .. (xr + 1)) {
          for (yi in (yr - 1) .. (yr + 1)) {
            val vec = CELL_2D [hash2D(seed, xi, yi) and 255]

            val vecX = xi -x + vec.x
            val vecY = yi -y + vec.y

            val newDistance =(abs(vecX) + abs(vecY)) + (vecX * vecX + vecY * vecY)

            if (newDistance < distance) {
              distance = newDistance
              xc = xi
              yc = yi
            }
          }
        }
      }
      else -> {
        for (xi in (xr - 1) .. (xr + 1)) {
          for (yi in (yr - 1) .. (yr + 1)) {
            val vec = CELL_2D [hash2D(seed, xi, yi) and 255]

            val vecX = xi -x + vec.x
            val vecY = yi -y + vec.y

            val newDistance = vecX * vecX +vecY * vecY

            if (newDistance < distance) {
              distance = newDistance
              xc = xi
              yc = yi
            }
          }
        }
      }
    }

    return when (cellularReturnType) {
      CellValue -> valCoord2D(0, xc, yc)

      NoiseLookup -> {
        val vec = CELL_2D [hash2D(seed, xc, yc) and 255]
        if (cellularNoiseLookup == null) {
          throw RuntimeException("noise: Forgot to set m_cellularNoiseLookup.")
        }
        cellularNoiseLookup!!.getNoise(xc + vec.x, yc + vec.y)
      }

      Distance -> distance - 1
      else -> 0f
    }
  }

  fun singleCellular2Edge(x: Float, y: Float): Float {
    val xr: Int = fastRound(x)
    val yr: Int = fastRound(y)

    var distance = 999999f
    var distance2 = 999999f

    when (cellularDistanceFunction) {
      Manhattan -> {
        for (xi in (xr - 1) .. (xr + 1)) {
          for (yi in (yr - 1) .. (yr + 1)) {
            val vec = CELL_2D [hash2D(seed, xi, yi) and 255]

            val vecX = xi -x + vec.x
            val vecY = yi -y + vec.y

            val newDistance = abs(vecX) + abs(vecY)

            distance2 = max(min(distance2, newDistance), distance)
            distance = min(distance, newDistance)
          }
        }
      }
      Natural -> {
        for (xi in (xr - 1) .. (xr + 1)) {
          for (yi in (yr - 1) .. (yr + 1)) {
            val vec = CELL_2D [hash2D(seed, xi, yi) and 255]

            val vecX = xi -x + vec.x
            val vecY = yi -y + vec.y

            val newDistance =(abs(vecX) + abs(vecY)) + (vecX * vecX + vecY * vecY)

            distance2 = max(min(distance2, newDistance), distance)
            distance = min(distance, newDistance)
          }
        }
      }
      else -> {
        for (xi in (xr - 1) .. (xr + 1)) {
          for (yi in (yr - 1) .. (yr + 1)) {
            val vec = CELL_2D [hash2D(seed, xi, yi) and 255]

            val vecX = xi -x + vec.x
            val vecY = yi -y + vec.y

            val newDistance = vecX * vecX +vecY * vecY

            distance2 = max(min(distance2, newDistance), distance)
            distance = min(distance, newDistance)
          }
        }
      }
    }

    return when (cellularReturnType) {
      Distance2 -> distance2 - 1
      Distance2Add -> distance2 + distance - 1
      Distance2Sub -> distance2 - distance - 1
      Distance2Mul -> distance2 * distance - 1
      Distance2Div -> distance / distance2 - 1
      else -> 0f
    }
  }

  fun gradientPerturb(v3: Vector3f) {
    singleGradientPerturb(seed, gradientPerturbAmp, frequency, v3)
  }

  fun gradientPerturbFractal(v3: Vector3f) {
    var seed = seed
    var amp = gradientPerturbAmp * fractalBounding
    var freq = frequency

    singleGradientPerturb(seed, amp, frequency, v3)

    for (i in 1 until octaves) {
      freq *= lacunarity
      amp *= gain
      singleGradientPerturb(++seed, amp, freq, v3)
    }
  }

  fun singleGradientPerturb(seed: Int, perturbAmp: Float, frequency: Float, v3: Vector3f) {
    val xf = v3.x * frequency
    val yf = v3.y * frequency
    val zf = v3.z * frequency

    val x0: Int = fastFloor(xf)
    val y0: Int = fastFloor(yf)
    val z0: Int = fastFloor(zf)
    val x1: Int = x0 + 1
    val y1: Int = y0 + 1
    val z1: Int = z0 + 1

    var xs = 0f
    var ys = 0f
    var zs = 0f

    when (interp) {
      Hermite -> {
        xs = interpHermiteFunc(xf - x0)
        ys = interpHermiteFunc(yf - y0)
        zs = interpHermiteFunc(zf - z0)
      }
      Quintic -> {
        xs = interpQuinticFunc(xf - x0)
        ys = interpQuinticFunc(yf - y0)
        zs = interpQuinticFunc(zf - z0)
      }
      else -> {
        xs = xf - x0
        ys = yf - y0
        zs = zf - z0
      }
    }

    var vec0 = CELL_3D[hash3D(seed, x0, y0, z0) and 255]
    var vec1 = CELL_3D[hash3D(seed, x1, y0, z0) and 255]

    var lx0x = lerp(vec0.x, vec1.x, xs)
    var ly0x = lerp(vec0.y, vec1.y, xs)
    var lz0x = lerp(vec0.z, vec1.z, xs)

    vec0 = CELL_3D[hash3D(seed, x0, y1, z0) and 255]
    vec1 = CELL_3D[hash3D(seed, x1, y1, z0) and 255]

    var lx1x = lerp(vec0.x, vec1.x, xs)
    var ly1x = lerp(vec0.y, vec1.y, xs)
    var lz1x = lerp(vec0.z, vec1.z, xs)

    val lx0y = lerp(lx0x, lx1x, ys)
    val ly0y = lerp(ly0x, ly1x, ys)
    val lz0y = lerp(lz0x, lz1x, ys)

    vec0 = CELL_3D[hash3D(seed, x0, y0, z1) and 255]
    vec1 = CELL_3D[hash3D(seed, x1, y0, z1) and 255]

    lx0x = lerp(vec0.x, vec1.x, xs)
    ly0x = lerp(vec0.y, vec1.y, xs)
    lz0x = lerp(vec0.z, vec1.z, xs)

    vec0 = CELL_3D[hash3D(seed, x0, y1, z1) and 255]
    vec1 = CELL_3D[hash3D(seed, x1, y1, z1) and 255]

    lx1x = lerp(vec0.x, vec1.x, xs)
    ly1x = lerp(vec0.y, vec1.y, xs)
    lz1x = lerp(vec0.z, vec1.z, xs)

    v3.x += lerp(lx0y, lerp(lx0x, lx1x, ys), zs) * perturbAmp
    v3.y += lerp(ly0y, lerp(ly0x, ly1x, ys), zs) * perturbAmp
    v3.z += lerp(lz0y, lerp(lz0x, lz1x, ys), zs) * perturbAmp
  }

  fun gradientPerturb(v2: Vector2f) {
    singleGradientPerturb(seed, gradientPerturbAmp, frequency, v2)
  }

  fun gradientPerturbFractal(v2: Vector2f) {
    var seed = seed
    var amp = gradientPerturbAmp * fractalBounding
    var freq = frequency

    singleGradientPerturb(seed, amp, frequency, v2)

    for (i in 1 until octaves) {
      freq *= lacunarity
      amp *= gain
      singleGradientPerturb(++seed, amp, freq, v2)
    }
  }

  fun singleGradientPerturb(seed: Int, perturbAmp: Float, frequency: Float, v2: Vector2f) {
    val xf = v2.x * frequency
    val yf = v2.y * frequency

    val x0: Int = fastFloor(xf)
    val y0: Int = fastFloor(yf)
    val x1: Int = x0 + 1
    val y1: Int = y0 + 1

    var xs = 0f
    var ys = 0f

    when (interp) {
      Hermite -> {
        xs = interpHermiteFunc(xf - x0)
        ys = interpHermiteFunc(yf - y0)
      }
      Quintic -> {
        xs = interpQuinticFunc(xf - x0)
        ys = interpQuinticFunc(yf - y0)
      }
      else -> {
        xs = xf - x0
        ys = yf - y0
      }
    }

    var vec0 = CELL_2D[hash2D(seed, x0, y0) and 255]
    var vec1 = CELL_2D[hash2D(seed, x1, y0) and 255]

    val lx0x = lerp(vec0.x, vec1.x, xs)
    val ly0x = lerp(vec0.y, vec1.y, xs)

    vec0 = CELL_2D[hash2D(seed, x0, y1) and 255]
    vec1 = CELL_2D[hash2D(seed, x1, y1) and 255]

    val lx1x = lerp(vec0.x, vec1.x, xs)
    val ly1x = lerp(vec0.y, vec1.y, xs)

    v2.x += lerp(lx0x, lx1x, ys) * perturbAmp
    v2.y += lerp(ly0x, ly1x, ys) * perturbAmp
  }

}
