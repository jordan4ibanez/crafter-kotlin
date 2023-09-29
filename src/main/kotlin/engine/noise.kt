package engine

import org.joml.Vector2f;
import org.joml.Vector3f;

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


  private var m_seed = 1337
  private var m_frequency = 0.01f
  private var m_interp = Interp.Quintic
  private var m_noiseType = NoiseType.Simplex;

  private var m_octaves = 3
  private var m_lacunarity = 2.0f
  private var m_gain = 0.5f
  private var m_fractalType = FractalType.FBM;

  private var m_fractalBounding = 0f;

  private var m_cellularDistanceFunction = CellularDistanceFunction.Euclidean;
  private var m_cellularReturnType = CellularReturnType.CellValue;
  private var m_cellularNoiseLookup: FastNoise? = null;

  private var m_gradientPerturbAmp = (1.0f / 0.45f)

  constructor(seed: Int) {
    m_seed = seed;
    CalculateFractalBounding();
  }

  // Returns a 0 float/double
  fun GetDecimalType(): Float {
    return 0f
  }

  // Returns the seed used by this object
  fun GetSeed(): Int {
    return m_seed;
  }

  // Sets seed used for all noise types
  // Default: 1337
  fun SetSeed(seed: Int) {
    m_seed = seed;
  }

  // Sets frequency for all noise types
  // Default: 0.01
  fun SetFrequency(frequency: Float) {
    m_frequency = frequency;
  }

  // Changes the interpolation method used to smooth between noise values
  // Possible interpolation methods (lowest to highest quality) :
  // - Linear
  // - Hermite
  // - Quintic
  // Used in Value, Gradient Noise and Position Perturbing
  // Default: Quintic
  fun SetInterp(interp: Interp) {
    m_interp = interp;
  }

  // Sets noise return type of GetNoise(...)
  // Default: Simplex
  fun SetNoiseType(noiseType: NoiseType) {
    m_noiseType = noiseType
  }

  // Sets octave count for all fractal noise types
  // Default: 3
  fun SetFractalOctaves(octaves: Int) {
    m_octaves = octaves;
    CalculateFractalBounding();
  }

  // Sets octave lacunarity for all fractal noise types
  // Default: 2.0
  fun SetFractalLacunarity(lacunarity: Float) {
    m_lacunarity = lacunarity;
  }

  // Sets octave gain for all fractal noise types
  // Default: 0.5
  fun SetFractalGain(gain: Float) {
    m_gain = gain;
    CalculateFractalBounding();
  }

  // Sets method for combining octaves in all fractal noise types
  // Default: FBM
  fun SetFractalType(fractalType: FractalType) {
    m_fractalType = fractalType;
  }

  // Sets return type from cellular noise calculations
  // Note: NoiseLookup requires another FastNoise object be set with SetCellularNoiseLookup() to function
  // Default: CellValue
  fun SetCellularDistanceFunction(cellularDistanceFunction: CellularDistanceFunction) {
    m_cellularDistanceFunction = cellularDistanceFunction;
  }

  // Sets distance function used in cellular noise calculations
  // Default: Euclidean
  fun SetCellularReturnType(cellularReturnType: CellularReturnType) {
    m_cellularReturnType = cellularReturnType;
  }

  // Noise used to calculate a cell value if cellular return type is NoiseLookup
  // The lookup value is acquired through GetNoise() so ensure you SetNoiseType() on the noise lookup, value, gradient or simplex is recommended
  fun SetCellularNoiseLookup(noise: FastNoise) {
    m_cellularNoiseLookup = noise;
  }

  // Sets the maximum perturb distance from original location when using GradientPerturb{Fractal}(...)
  // Default: 1.0
  fun SetGradientPerturbAmp(gradientPerturbAmp: Float) {
    m_gradientPerturbAmp = gradientPerturbAmp / 0.45f
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
    Float2(-0.4313539279f, 0.1281943404f), Float2(-0.1733316799f, 0.415278375f), Float2(-0.2821957395f, -0.3505218461f), Float2(-0.2806473808f, 0.3517627718f), Float2(0.3125508975f, -0.3237467165f), Float2(0.3383018443f, -0.2967353402f), Float2(-0.4393982022f, -0.09710417025f), Float2(-0.4460443703f, -0.05953502905f),
    Float2(-0.302223039f, 0.3334085102f), Float2(-0.212681052f, -0.3965687458f), Float2(-0.2991156529f, 0.3361990872f), Float2(0.2293323691f, 0.3871778202f), Float2(0.4475439151f, -0.04695150755f), Float2(0.1777518f, 0.41340573f), Float2(0.1688522499f, -0.4171197882f), Float2(-0.0976597166f, 0.4392750616f),
    Float2(0.08450188373f, 0.4419948321f), Float2(-0.4098760448f, -0.1857461384f), Float2(0.3476585782f, -0.2857157906f), Float2(-0.3350670039f, -0.30038326f), Float2(0.2298190031f, -0.3868891648f), Float2(-0.01069924099f, 0.449872789f), Float2(-0.4460141246f, -0.05976119672f), Float2(0.3650293864f, 0.2631606867f),
    Float2(-0.349479423f, 0.2834856838f), Float2(-0.4122720642f, 0.1803655873f), Float2(-0.267327811f, 0.3619887311f), Float2(0.322124041f, -0.3142230135f), Float2(0.2880445931f, -0.3457315612f), Float2(0.3892170926f, -0.2258540565f), Float2(0.4492085018f, -0.02667811596f), Float2(-0.4497724772f, 0.01430799601f),
    Float2(0.1278175387f, -0.4314657307f), Float2(-0.03572100503f, 0.4485799926f), Float2(-0.4297407068f, -0.1335025276f), Float2(-0.3217817723f, 0.3145735065f), Float2(-0.3057158873f, 0.3302087162f), Float2(-0.414503978f, 0.1751754899f), Float2(-0.3738139881f, 0.2505256519f), Float2(0.2236891408f, -0.3904653228f),
    Float2(0.002967775577f, -0.4499902136f), Float2(0.1747128327f, -0.4146991995f), Float2(-0.4423772489f, -0.08247647938f), Float2(-0.2763960987f, -0.355112935f), Float2(-0.4019385906f, -0.2023496216f), Float2(0.3871414161f, -0.2293938184f), Float2(-0.430008727f, 0.1326367019f), Float2(-0.03037574274f, -0.4489736231f),
    Float2(-0.3486181573f, 0.2845441624f), Float2(0.04553517144f, -0.4476902368f), Float2(-0.0375802926f, 0.4484280562f), Float2(0.3266408905f, 0.3095250049f), Float2(0.06540017593f, -0.4452222108f), Float2(0.03409025829f, 0.448706869f), Float2(-0.4449193635f, 0.06742966669f), Float2(-0.4255936157f, -0.1461850686f),
    Float2(0.449917292f, 0.008627302568f), Float2(0.05242606404f, 0.4469356864f), Float2(-0.4495305179f, -0.02055026661f), Float2(-0.1204775703f, 0.4335725488f), Float2(-0.341986385f, -0.2924813028f), Float2(0.3865320182f, 0.2304191809f), Float2(0.04506097811f, -0.447738214f), Float2(-0.06283465979f, 0.4455915232f),
    Float2(0.3932600341f, -0.2187385324f), Float2(0.4472261803f, -0.04988730975f), Float2(0.3753571011f, -0.2482076684f), Float2(-0.273662295f, 0.357223947f), Float2(0.1700461538f, 0.4166344988f), Float2(0.4102692229f, 0.1848760794f), Float2(0.323227187f, -0.3130881435f), Float2(-0.2882310238f, -0.3455761521f),
    Float2(0.2050972664f, 0.4005435199f), Float2(0.4414085979f, -0.08751256895f), Float2(-0.1684700334f, 0.4172743077f), Float2(-0.003978032396f, 0.4499824166f), Float2(-0.2055133639f, 0.4003301853f), Float2(-0.006095674897f, -0.4499587123f), Float2(-0.1196228124f, -0.4338091548f), Float2(0.3901528491f, -0.2242337048f),
    Float2(0.01723531752f, 0.4496698165f), Float2(-0.3015070339f, 0.3340561458f), Float2(-0.01514262423f, -0.4497451511f), Float2(-0.4142574071f, -0.1757577897f), Float2(-0.1916377265f, -0.4071547394f), Float2(0.3749248747f, 0.2488600778f), Float2(-0.2237774255f, 0.3904147331f), Float2(-0.4166343106f, -0.1700466149f),
    Float2(0.3619171625f, 0.267424695f), Float2(0.1891126846f, -0.4083336779f), Float2(-0.3127425077f, 0.323561623f), Float2(-0.3281807787f, 0.307891826f), Float2(-0.2294806661f, 0.3870899429f), Float2(-0.3445266136f, 0.2894847362f), Float2(-0.4167095422f, -0.1698621719f), Float2(-0.257890321f, -0.3687717212f),
    Float2(-0.3612037825f, 0.2683874578f), Float2(0.2267996491f, 0.3886668486f), Float2(0.207157062f, 0.3994821043f), Float2(0.08355176718f, -0.4421754202f), Float2(-0.4312233307f, 0.1286329626f), Float2(0.3257055497f, 0.3105090899f), Float2(0.177701095f, -0.4134275279f), Float2(-0.445182522f, 0.06566979625f),
    Float2(0.3955143435f, 0.2146355146f), Float2(-0.4264613988f, 0.1436338239f), Float2(-0.3793799665f, -0.2420141339f), Float2(0.04617599081f, -0.4476245948f), Float2(-0.371405428f, -0.2540826796f), Float2(0.2563570295f, -0.3698392535f), Float2(0.03476646309f, 0.4486549822f), Float2(-0.3065454405f, 0.3294387544f),
    Float2(-0.2256979823f, 0.3893076172f), Float2(0.4116448463f, -0.1817925206f), Float2(-0.2907745828f, -0.3434387019f), Float2(0.2842278468f, -0.348876097f), Float2(0.3114589359f, -0.3247973695f), Float2(0.4464155859f, -0.0566844308f), Float2(-0.3037334033f, -0.3320331606f), Float2(0.4079607166f, 0.1899159123f),
    Float2(-0.3486948919f, -0.2844501228f), Float2(0.3264821436f, 0.3096924441f), Float2(0.3211142406f, 0.3152548881f), Float2(0.01183382662f, 0.4498443737f), Float2(0.4333844092f, 0.1211526057f), Float2(0.3118668416f, 0.324405723f), Float2(-0.272753471f, 0.3579183483f), Float2(-0.422228622f, -0.1556373694f),
    Float2(-0.1009700099f, -0.4385260051f), Float2(-0.2741171231f, -0.3568750521f), Float2(-0.1465125133f, 0.4254810025f), Float2(0.2302279044f, -0.3866459777f), Float2(-0.3699435608f, 0.2562064828f), Float2(0.105700352f, -0.4374099171f), Float2(-0.2646713633f, 0.3639355292f), Float2(0.3521828122f, 0.2801200935f),
    Float2(-0.1864187807f, -0.4095705534f), Float2(0.1994492955f, -0.4033856449f), Float2(0.3937065066f, 0.2179339044f), Float2(-0.3226158377f, 0.3137180602f), Float2(0.3796235338f, 0.2416318948f), Float2(0.1482921929f, 0.4248640083f), Float2(-0.407400394f, 0.1911149365f), Float2(0.4212853031f, 0.1581729856f),
    Float2(-0.2621297173f, 0.3657704353f), Float2(-0.2536986953f, -0.3716678248f), Float2(-0.2100236383f, 0.3979825013f), Float2(0.3624152444f, 0.2667493029f), Float2(-0.3645038479f, -0.2638881295f), Float2(0.2318486784f, 0.3856762766f), Float2(-0.3260457004f, 0.3101519002f), Float2(-0.2130045332f, -0.3963950918f),
    Float2(0.3814998766f, -0.2386584257f), Float2(-0.342977305f, 0.2913186713f), Float2(-0.4355865605f, 0.1129794154f), Float2(-0.2104679605f, 0.3977477059f), Float2(0.3348364681f, -0.3006402163f), Float2(0.3430468811f, 0.2912367377f), Float2(-0.2291836801f, -0.3872658529f), Float2(0.2547707298f, -0.3709337882f),
    Float2(0.4236174945f, -0.151816397f), Float2(-0.15387742f, 0.4228731957f), Float2(-0.4407449312f, 0.09079595574f), Float2(-0.06805276192f, -0.444824484f), Float2(0.4453517192f, -0.06451237284f), Float2(0.2562464609f, -0.3699158705f), Float2(0.3278198355f, -0.3082761026f), Float2(-0.4122774207f, -0.1803533432f),
    Float2(0.3354090914f, -0.3000012356f), Float2(0.446632869f, -0.05494615882f), Float2(-0.1608953296f, 0.4202531296f), Float2(-0.09463954939f, 0.4399356268f), Float2(-0.02637688324f, -0.4492262904f), Float2(0.447102804f, -0.05098119915f), Float2(-0.4365670908f, 0.1091291678f), Float2(-0.3959858651f, 0.2137643437f),
    Float2(-0.4240048207f, -0.1507312575f), Float2(-0.3882794568f, 0.2274622243f), Float2(-0.4283652566f, -0.1378521198f), Float2(0.3303888091f, 0.305521251f), Float2(0.3321434919f, -0.3036127481f), Float2(-0.413021046f, -0.1786438231f), Float2(0.08403060337f, -0.4420846725f), Float2(-0.3822882919f, 0.2373934748f),
    Float2(-0.3712395594f, -0.2543249683f), Float2(0.4472363971f, -0.04979563372f), Float2(-0.4466591209f, 0.05473234629f), Float2(0.0486272539f, -0.4473649407f), Float2(-0.4203101295f, -0.1607463688f), Float2(0.2205360833f, 0.39225481f), Float2(-0.3624900666f, 0.2666476169f), Float2(-0.4036086833f, -0.1989975647f),
    Float2(0.2152727807f, 0.3951678503f), Float2(-0.4359392962f, -0.1116106179f), Float2(0.4178354266f, 0.1670735057f), Float2(0.2007630161f, 0.4027334247f), Float2(-0.07278067175f, -0.4440754146f), Float2(0.3644748615f, -0.2639281632f), Float2(-0.4317451775f, 0.126870413f), Float2(-0.297436456f, 0.3376855855f),
    Float2(-0.2998672222f, 0.3355289094f), Float2(-0.2673674124f, 0.3619594822f), Float2(0.2808423357f, 0.3516071423f), Float2(0.3498946567f, 0.2829730186f), Float2(-0.2229685561f, 0.390877248f), Float2(0.3305823267f, 0.3053118493f), Float2(-0.2436681211f, -0.3783197679f), Float2(-0.03402776529f, 0.4487116125f),
    Float2(-0.319358823f, 0.3170330301f), Float2(0.4454633477f, -0.06373700535f), Float2(0.4483504221f, 0.03849544189f), Float2(-0.4427358436f, -0.08052932871f), Float2(0.05452298565f, 0.4466847255f), Float2(-0.2812560807f, 0.3512762688f), Float2(0.1266696921f, 0.4318041097f), Float2(-0.3735981243f, 0.2508474468f),
    Float2(0.2959708351f, -0.3389708908f), Float2(-0.3714377181f, 0.254035473f), Float2(-0.404467102f, -0.1972469604f), Float2(0.1636165687f, -0.419201167f), Float2(0.3289185495f, -0.3071035458f), Float2(-0.2494824991f, -0.3745109914f), Float2(0.03283133272f, 0.4488007393f), Float2(-0.166306057f, -0.4181414777f),
    Float2(-0.106833179f, 0.4371346153f), Float2(0.06440260376f, -0.4453676062f), Float2(-0.4483230967f, 0.03881238203f), Float2(-0.421377757f, -0.1579265206f), Float2(0.05097920662f, -0.4471030312f), Float2(0.2050584153f, -0.4005634111f), Float2(0.4178098529f, -0.167137449f), Float2(-0.3565189504f, -0.2745801121f),
    Float2(0.4478398129f, 0.04403977727f), Float2(-0.3399999602f, -0.2947881053f), Float2(0.3767121994f, 0.2461461331f), Float2(-0.3138934434f, 0.3224451987f), Float2(-0.1462001792f, -0.4255884251f), Float2(0.3970290489f, -0.2118205239f), Float2(0.4459149305f, -0.06049689889f), Float2(-0.4104889426f, -0.1843877112f),
    Float2(0.1475103971f, -0.4251360756f), Float2(0.09258030352f, 0.4403735771f), Float2(-0.1589664637f, -0.4209865359f), Float2(0.2482445008f, 0.3753327428f), Float2(0.4383624232f, -0.1016778537f), Float2(0.06242802956f, 0.4456486745f), Float2(0.2846591015f, -0.3485243118f), Float2(-0.344202744f, -0.2898697484f),
    Float2(0.1198188883f, -0.4337550392f), Float2(-0.243590703f, 0.3783696201f), Float2(0.2958191174f, -0.3391033025f), Float2(-0.1164007991f, 0.4346847754f), Float2(0.1274037151f, -0.4315881062f), Float2(0.368047306f, 0.2589231171f), Float2(0.2451436949f, 0.3773652989f), Float2(-0.4314509715f, 0.12786735f),
  )

  private val CELL_3D =
  arrayOf(
    Float3(0.1453787434f, -0.4149781685f, -0.0956981749f), Float3(-0.01242829687f, -0.1457918398f, -0.4255470325f), Float3(0.2877979582f, -0.02606483451f, -0.3449535616f), Float3(-0.07732986802f, 0.2377094325f, 0.3741848704f), Float3(0.1107205875f, -0.3552302079f, -0.2530858567f), Float3(0.2755209141f, 0.2640521179f, -0.238463215f), Float3(0.294168941f, 0.1526064594f, 0.3044271714f), Float3(0.4000921098f, -0.2034056362f, 0.03244149937f),
    Float3(-0.1697304074f, 0.3970864695f, -0.1265461359f), Float3(-0.1483224484f, -0.3859694688f, 0.1775613147f), Float3(0.2623596946f, -0.2354852944f, 0.2796677792f), Float3(-0.2709003183f, 0.3505271138f, -0.07901746678f), Float3(-0.03516550699f, 0.3885234328f, 0.2243054374f), Float3(-0.1267712655f, 0.1920044036f, 0.3867342179f), Float3(0.02952021915f, 0.4409685861f, 0.08470692262f), Float3(-0.2806854217f, -0.266996757f, 0.2289725438f),
    Float3(-0.171159547f, 0.2141185563f, 0.3568720405f), Float3(0.2113227183f, 0.3902405947f, -0.07453178509f), Float3(-0.1024352839f, 0.2128044156f, -0.3830421561f), Float3(-0.3304249877f, -0.1566986703f, 0.2622305365f), Float3(0.2091111325f, 0.3133278055f, -0.2461670583f), Float3(0.344678154f, -0.1944240454f, -0.2142341261f), Float3(0.1984478035f, -0.3214342325f, -0.2445373252f), Float3(-0.2929008603f, 0.2262915116f, 0.2559320961f),
    Float3(-0.1617332831f, 0.006314769776f, -0.4198838754f), Float3(-0.3582060271f, -0.148303178f, -0.2284613961f), Float3(-0.1852067326f, -0.3454119342f, -0.2211087107f), Float3(0.3046301062f, 0.1026310383f, 0.314908508f), Float3(-0.03816768434f, -0.2551766358f, -0.3686842991f), Float3(-0.4084952196f, 0.1805950793f, 0.05492788837f), Float3(-0.02687443361f, -0.2749741471f, 0.3551999201f), Float3(-0.03801098351f, 0.3277859044f, 0.3059600725f),
    Float3(0.2371120802f, 0.2900386767f, -0.2493099024f), Float3(0.4447660503f, 0.03946930643f, 0.05590469027f), Float3(0.01985147278f, -0.01503183293f, -0.4493105419f), Float3(0.4274339143f, 0.03345994256f, -0.1366772882f), Float3(-0.2072988631f, 0.2871414597f, -0.2776273824f), Float3(-0.3791240978f, 0.1281177671f, 0.2057929936f), Float3(-0.2098721267f, -0.1007087278f, -0.3851122467f), Float3(0.01582798878f, 0.4263894424f, 0.1429738373f),
    Float3(-0.1888129464f, -0.3160996813f, -0.2587096108f), Float3(0.1612988974f, -0.1974805082f, -0.3707885038f), Float3(-0.08974491322f, 0.229148752f, -0.3767448739f), Float3(0.07041229526f, 0.4150230285f, -0.1590534329f), Float3(-0.1082925611f, -0.1586061639f, 0.4069604477f), Float3(0.2474100658f, -0.3309414609f, 0.1782302128f), Float3(-0.1068836661f, -0.2701644537f, -0.3436379634f), Float3(0.2396452163f, 0.06803600538f, -0.3747549496f),
    Float3(-0.3063886072f, 0.2597428179f, 0.2028785103f), Float3(0.1593342891f, -0.3114350249f, -0.2830561951f), Float3(0.2709690528f, 0.1412648683f, -0.3303331794f), Float3(-0.1519780427f, 0.3623355133f, 0.2193527988f), Float3(0.1699773681f, 0.3456012883f, 0.2327390037f), Float3(-0.1986155616f, 0.3836276443f, -0.1260225743f), Float3(-0.1887482106f, -0.2050154888f, -0.353330953f), Float3(0.2659103394f, 0.3015631259f, -0.2021172246f),
    Float3(-0.08838976154f, -0.4288819642f, -0.1036702021f), Float3(-0.04201869311f, 0.3099592485f, 0.3235115047f), Float3(-0.3230334656f, 0.201549922f, -0.2398478873f), Float3(0.2612720941f, 0.2759854499f, -0.2409749453f), Float3(0.385713046f, 0.2193460345f, 0.07491837764f), Float3(0.07654967953f, 0.3721732183f, 0.241095919f), Float3(0.4317038818f, -0.02577753072f, 0.1243675091f), Float3(-0.2890436293f, -0.3418179959f, -0.04598084447f),
    Float3(-0.2201947582f, 0.383023377f, -0.08548310451f), Float3(0.4161322773f, -0.1669634289f, -0.03817251927f), Float3(0.2204718095f, 0.02654238946f, -0.391391981f), Float3(-0.1040307469f, 0.3890079625f, -0.2008741118f), Float3(-0.1432122615f, 0.371614387f, -0.2095065525f), Float3(0.3978380468f, -0.06206669342f, 0.2009293758f), Float3(-0.2599274663f, 0.2616724959f, -0.2578084893f), Float3(0.4032618332f, -0.1124593585f, 0.1650235939f),
    Float3(-0.08953470255f, -0.3048244735f, 0.3186935478f), Float3(0.118937202f, -0.2875221847f, 0.325092195f), Float3(0.02167047076f, -0.03284630549f, -0.4482761547f), Float3(-0.3411343612f, 0.2500031105f, 0.1537068389f), Float3(0.3162964612f, 0.3082064153f, -0.08640228117f), Float3(0.2355138889f, -0.3439334267f, -0.1695376245f), Float3(-0.02874541518f, -0.3955933019f, 0.2125550295f), Float3(-0.2461455173f, 0.02020282325f, -0.3761704803f),
    Float3(0.04208029445f, -0.4470439576f, 0.02968078139f), Float3(0.2727458746f, 0.2288471896f, -0.2752065618f), Float3(-0.1347522818f, -0.02720848277f, -0.4284874806f), Float3(0.3829624424f, 0.1231931484f, -0.2016512234f), Float3(-0.3547613644f, 0.1271702173f, 0.2459107769f), Float3(0.2305790207f, 0.3063895591f, 0.2354968222f), Float3(-0.08323845599f, -0.1922245118f, 0.3982726409f), Float3(0.2993663085f, -0.2619918095f, -0.2103333191f),
    Float3(-0.2154865723f, 0.2706747713f, 0.287751117f), Float3(0.01683355354f, -0.2680655787f, -0.3610505186f), Float3(0.05240429123f, 0.4335128183f, -0.1087217856f), Float3(0.00940104872f, -0.4472890582f, 0.04841609928f), Float3(0.3465688735f, 0.01141914583f, -0.2868093776f), Float3(-0.3706867948f, -0.2551104378f, 0.003156692623f), Float3(0.2741169781f, 0.2139972417f, -0.2855959784f), Float3(0.06413433865f, 0.1708718512f, 0.4113266307f),
    Float3(-0.388187972f, -0.03973280434f, -0.2241236325f), Float3(0.06419469312f, -0.2803682491f, 0.3460819069f), Float3(-0.1986120739f, -0.3391173584f, 0.2192091725f), Float3(-0.203203009f, -0.3871641506f, 0.1063600375f), Float3(-0.1389736354f, -0.2775901578f, -0.3257760473f), Float3(-0.06555641638f, 0.342253257f, -0.2847192729f), Float3(-0.2529246486f, -0.2904227915f, 0.2327739768f), Float3(0.1444476522f, 0.1069184044f, 0.4125570634f),
    Float3(-0.3643780054f, -0.2447099973f, -0.09922543227f), Float3(0.4286142488f, -0.1358496089f, -0.01829506817f), Float3(0.165872923f, -0.3136808464f, -0.2767498872f), Float3(0.2219610524f, -0.3658139958f, 0.1393320198f), Float3(0.04322940318f, -0.3832730794f, 0.2318037215f), Float3(-0.08481269795f, -0.4404869674f, -0.03574965489f), Float3(0.1822082075f, -0.3953259299f, 0.1140946023f), Float3(-0.3269323334f, 0.3036542563f, 0.05838957105f),
    Float3(-0.4080485344f, 0.04227858267f, -0.184956522f), Float3(0.2676025294f, -0.01299671652f, 0.36155217f), Float3(0.3024892441f, -0.1009990293f, -0.3174892964f), Float3(0.1448494052f, 0.425921681f, -0.0104580805f), Float3(0.4198402157f, 0.08062320474f, 0.1404780841f), Float3(-0.3008872161f, -0.333040905f, -0.03241355801f), Float3(0.3639310428f, -0.1291284382f, -0.2310412139f), Float3(0.3295806598f, 0.0184175994f, -0.3058388149f),
    Float3(0.2776259487f, -0.2974929052f, -0.1921504723f), Float3(0.4149000507f, -0.144793182f, -0.09691688386f), Float3(0.145016715f, -0.0398992945f, 0.4241205002f), Float3(0.09299023471f, -0.299732164f, -0.3225111565f), Float3(0.1028907093f, -0.361266869f, 0.247789732f), Float3(0.2683057049f, -0.07076041213f, -0.3542668666f), Float3(-0.4227307273f, -0.07933161816f, -0.1323073187f), Float3(-0.1781224702f, 0.1806857196f, -0.3716517945f),
    Float3(0.4390788626f, -0.02841848598f, -0.09435116353f), Float3(0.2972583585f, 0.2382799621f, -0.2394997452f), Float3(-0.1707002821f, 0.2215845691f, 0.3525077196f), Float3(0.3806686614f, 0.1471852559f, -0.1895464869f), Float3(-0.1751445661f, -0.274887877f, 0.3102596268f), Float3(-0.2227237566f, -0.2316778837f, 0.3149912482f), Float3(0.1369633021f, 0.1341343041f, -0.4071228836f), Float3(-0.3529503428f, -0.2472893463f, -0.129514612f),
    Float3(-0.2590744185f, -0.2985577559f, -0.2150435121f), Float3(-0.3784019401f, 0.2199816631f, -0.1044989934f), Float3(-0.05635805671f, 0.1485737441f, 0.4210102279f), Float3(0.3251428613f, 0.09666046873f, -0.2957006485f), Float3(-0.4190995804f, 0.1406751354f, -0.08405978803f), Float3(-0.3253150961f, -0.3080335042f, -0.04225456877f), Float3(0.2857945863f, -0.05796152095f, 0.3427271751f), Float3(-0.2733604046f, 0.1973770973f, -0.2980207554f),
    Float3(0.219003657f, 0.2410037886f, -0.3105713639f), Float3(0.3182767252f, -0.271342949f, 0.1660509868f), Float3(-0.03222023115f, -0.3331161506f, -0.300824678f), Float3(-0.3087780231f, 0.1992794134f, -0.2596995338f), Float3(-0.06487611647f, -0.4311322747f, 0.1114273361f), Float3(0.3921171432f, -0.06294284106f, -0.2116183942f), Float3(-0.1606404506f, -0.358928121f, -0.2187812825f), Float3(-0.03767771199f, -0.2290351443f, 0.3855169162f),
    Float3(0.1394866832f, -0.3602213994f, 0.2308332918f), Float3(-0.4345093872f, 0.005751117145f, 0.1169124335f), Float3(-0.1044637494f, 0.4168128432f, -0.1336202785f), Float3(0.2658727501f, 0.2551943237f, 0.2582393035f), Float3(0.2051461999f, 0.1975390727f, 0.3484154868f), Float3(-0.266085566f, 0.23483312f, 0.2766800993f), Float3(0.07849405464f, -0.3300346342f, -0.2956616708f), Float3(-0.2160686338f, 0.05376451292f, -0.3910546287f),
    Float3(-0.185779186f, 0.2148499206f, 0.3490352499f), Float3(0.02492421743f, -0.3229954284f, -0.3123343347f), Float3(-0.120167831f, 0.4017266681f, 0.1633259825f), Float3(-0.02160084693f, -0.06885389554f, 0.4441762538f), Float3(0.2597670064f, 0.3096300784f, 0.1978643903f), Float3(-0.1611553854f, -0.09823036005f, 0.4085091653f), Float3(-0.3278896792f, 0.1461670309f, 0.2713366126f), Float3(0.2822734956f, 0.03754421121f, -0.3484423997f),
    Float3(0.03169341113f, 0.347405252f, -0.2842624114f), Float3(0.2202613604f, -0.3460788041f, -0.1849713341f), Float3(0.2933396046f, 0.3031973659f, 0.1565989581f), Float3(-0.3194922995f, 0.2453752201f, -0.200538455f), Float3(-0.3441586045f, -0.1698856132f, -0.2349334659f), Float3(0.2703645948f, -0.3574277231f, 0.04060059933f), Float3(0.2298568861f, 0.3744156221f, 0.0973588921f), Float3(0.09326603877f, -0.3170108894f, 0.3054595587f),
    Float3(-0.1116165319f, -0.2985018719f, 0.3177080142f), Float3(0.2172907365f, -0.3460005203f, -0.1885958001f), Float3(0.1991339479f, 0.3820341668f, -0.1299829458f), Float3(-0.0541918155f, -0.2103145071f, 0.39412061f), Float3(0.08871336998f, 0.2012117383f, 0.3926114802f), Float3(0.2787673278f, 0.3505404674f, 0.04370535101f), Float3(-0.322166438f, 0.3067213525f, 0.06804996813f), Float3(-0.4277366384f, 0.132066775f, 0.04582286686f),
    Float3(0.240131882f, -0.1612516055f, 0.344723946f), Float3(0.1448607981f, -0.2387819045f, 0.3528435224f), Float3(-0.3837065682f, -0.2206398454f, 0.08116235683f), Float3(-0.4382627882f, -0.09082753406f, -0.04664855374f), Float3(-0.37728353f, 0.05445141085f, 0.2391488697f), Float3(0.1259579313f, 0.348394558f, 0.2554522098f), Float3(-0.1406285511f, -0.270877371f, -0.3306796947f), Float3(-0.1580694418f, 0.4162931958f, -0.06491553533f),
    Float3(0.2477612106f, -0.2927867412f, -0.2353514536f), Float3(0.2916132853f, 0.3312535401f, 0.08793624968f), Float3(0.07365265219f, -0.1666159848f, 0.411478311f), Float3(-0.26126526f, -0.2422237692f, 0.2748965434f), Float3(-0.3721862032f, 0.252790166f, 0.008634938242f), Float3(-0.3691191571f, -0.255281188f, 0.03290232422f), Float3(0.2278441737f, -0.3358364886f, 0.1944244981f), Float3(0.363398169f, -0.2310190248f, 0.1306597909f),
    Float3(-0.304231482f, -0.2698452035f, 0.1926830856f), Float3(-0.3199312232f, 0.316332536f, -0.008816977938f), Float3(0.2874852279f, 0.1642275508f, -0.304764754f), Float3(-0.1451096801f, 0.3277541114f, -0.2720669462f), Float3(0.3220090754f, 0.0511344108f, 0.3101538769f), Float3(-0.1247400865f, -0.04333605335f, -0.4301882115f), Float3(-0.2829555867f, -0.3056190617f, -0.1703910946f), Float3(0.1069384374f, 0.3491024667f, -0.2630430352f),
    Float3(-0.1420661144f, -0.3055376754f, -0.2982682484f), Float3(-0.250548338f, 0.3156466809f, -0.2002316239f), Float3(0.3265787872f, 0.1871229129f, 0.2466400438f), Float3(0.07646097258f, -0.3026690852f, 0.324106687f), Float3(0.3451771584f, 0.2757120714f, -0.0856480183f), Float3(0.298137964f, 0.2852657134f, 0.179547284f), Float3(0.2812250376f, 0.3466716415f, 0.05684409612f), Float3(0.4390345476f, -0.09790429955f, -0.01278335452f),
    Float3(0.2148373234f, 0.1850172527f, 0.3494474791f), Float3(0.2595421179f, -0.07946825393f, 0.3589187731f), Float3(0.3182823114f, -0.307355516f, -0.08203022006f), Float3(-0.4089859285f, -0.04647718411f, 0.1818526372f), Float3(-0.2826749061f, 0.07417482322f, 0.3421885344f), Float3(0.3483864637f, 0.225442246f, -0.1740766085f), Float3(-0.3226415069f, -0.1420585388f, -0.2796816575f), Float3(0.4330734858f, -0.118868561f, -0.02859407492f),
    Float3(-0.08717822568f, -0.3909896417f, -0.2050050172f), Float3(-0.2149678299f, 0.3939973956f, -0.03247898316f), Float3(-0.2687330705f, 0.322686276f, -0.1617284888f), Float3(0.2105665099f, -0.1961317136f, -0.3459683451f), Float3(0.4361845915f, -0.1105517485f, 0.004616608544f), Float3(0.05333333359f, -0.313639498f, -0.3182543336f), Float3(-0.05986216652f, 0.1361029153f, -0.4247264031f), Float3(0.3664988455f, 0.2550543014f, -0.05590974511f),
    Float3(-0.2341015558f, -0.182405731f, 0.3382670703f), Float3(-0.04730947785f, -0.4222150243f, -0.1483114513f), Float3(-0.2391566239f, -0.2577696514f, -0.2808182972f), Float3(-0.1242081035f, 0.4256953395f, -0.07652336246f), Float3(0.2614832715f, -0.3650179274f, 0.02980623099f), Float3(-0.2728794681f, -0.3499628774f, 0.07458404908f), Float3(0.007892900508f, -0.1672771315f, 0.4176793787f), Float3(-0.01730330376f, 0.2978486637f, -0.3368779738f),
    Float3(0.2054835762f, -0.3252600376f, -0.2334146693f), Float3(-0.3231994983f, 0.1564282844f, -0.2712420987f), Float3(-0.2669545963f, 0.2599343665f, -0.2523278991f), Float3(-0.05554372779f, 0.3170813944f, -0.3144428146f), Float3(-0.2083935713f, -0.310922837f, -0.2497981362f), Float3(0.06989323478f, -0.3156141536f, 0.3130537363f), Float3(0.3847566193f, -0.1605309138f, -0.1693876312f), Float3(-0.3026215288f, -0.3001537679f, -0.1443188342f),
    Float3(0.3450735512f, 0.08611519592f, 0.2756962409f), Float3(0.1814473292f, -0.2788782453f, -0.3029914042f), Float3(-0.03855010448f, 0.09795110726f, 0.4375151083f), Float3(0.3533670318f, 0.2665752752f, 0.08105160988f), Float3(-0.007945601311f, 0.140359426f, -0.4274764309f), Float3(0.4063099273f, -0.1491768253f, -0.1231199324f), Float3(-0.2016773589f, 0.008816271194f, -0.4021797064f), Float3(-0.07527055435f, -0.425643481f, -0.1251477955f),
  )


  private fun FastFloor(f: Float): Int {
    return if (f >= 0) f.toInt() else f.toInt() - 1
  }


  private fun FastRound(f: Float): Int {
    return if (f >= 0) (f + 0.5f).toInt() else (f - 0.5f).toInt()
  }


  private fun Lerp(a: Float, b: Float, t: Float): Float {
    return a + t * (b - a);
  }


  private fun InterpHermiteFunc(t: Float): Float {
    return t * t * (3 - 2 * t);
  }


  private fun InterpQuinticFunc(t: Float): Float {
    return t * t * t * (t * (t * 6 - 15) + 10);
  }


  private fun CubicLerp(a: Float, b: Float, c: Float, d: Float, t: Float): Float {
    val p = (d - c) - (a - b);
    return t * t * t * p + t * t * ((a - b) - p) + t * (c - a) + b;
  }

  private fun CalculateFractalBounding() {
    var amp = m_gain;
    var ampFractal = 1f;
    for (i in 1 until m_octaves) {
      ampFractal += amp
      amp *= m_gain;
    }
    m_fractalBounding = 1f / ampFractal;
  }

  // Hashing
  private val X_PRIME = 1619
  private val Y_PRIME = 31337
  private val Z_PRIME = 6971
  private val W_PRIME = 1013

  private fun Hash2D(seed: Int, x: Int, y: Int): Int {
    var hash = seed
    hash = hash xor X_PRIME * x
    hash = hash xor Y_PRIME * y

    hash = hash * hash * hash * 60493
    hash = (hash shr 13) xor hash

    return hash;
  }

  private fun Hash3D(seed: Int, x: Int, y: Int, z: Int): Int {
    var hash = seed
    hash = hash xor X_PRIME * x
    hash = hash xor Y_PRIME * y
    hash = hash xor Z_PRIME * z

    hash = hash * hash * hash * 60493
    hash = (hash shr 13) xor hash

    return hash
  }

  private fun Hash4D(seed: Int, x: Int, y: Int, z: Int, w: Int): Int {
    var hash = seed
    hash = hash xor X_PRIME * x
    hash = hash xor Y_PRIME * y
    hash = hash xor Z_PRIME * z
    hash = hash xor W_PRIME * w

    hash *= hash * hash * 60493
    hash = (hash shr 13) xor hash

    return hash
  }

  private fun ValCoord2D(seed: Int, x: Int, y: Int): Float {
    var n = seed;
    n = n xor X_PRIME * x;
    n = n xor Y_PRIME * y;

    return (n * n * n * 60493) / 2147483648.0f;
  }

  private fun ValCoord3D(seed: Int, x: Int, y: Int, z: Int): Float {
    var n = seed;
    n = n xor X_PRIME * x;
    n = n xor Y_PRIME * y;
    n = n xor Z_PRIME * z;

    return (n * n * n * 60493) / 2147483648.0f;
  }

  private fun ValCoord4D(seed: Int, x: Int, y: Int, z: Int, w: Int): Float {
    var n = seed;
    n = n xor X_PRIME * x;
    n = n xor Y_PRIME * y;
    n = n xor Z_PRIME * z;
    n = n xor W_PRIME * w;

    return (n * n * n * 60493) / 2147483648.0f;
  }

  private fun GradCoord2D(seed: Int, x: Int, y: Int, xd: Float, yd: Float): Float {
    var hash = seed;
    hash = hash xor X_PRIME * x;
    hash = hash xor Y_PRIME * y;

    hash = hash * hash * hash * 60493;
    hash = (hash shr 13) xor hash;

    val g = GRAD_2D[hash and 7];

    return xd * g.x + yd * g.y;
  }

  private fun GradCoord3D(seed: Int, x: Int, y: Int, z: Int, xd: Float, yd: Float, zd: Float): Float {
    var hash = seed;
    hash = hash xor X_PRIME * x;
    hash = hash xor Y_PRIME * y;
    hash = hash xor Z_PRIME * z;

    hash = hash * hash * hash * 60493;
    hash = (hash shr 13) xor hash;

    val g = GRAD_3D[hash and 15];

    return xd * g.x + yd * g.y + zd * g.z;
  }

  private fun GradCoord4D(seed: Int, x: Int, y: Int, z: Int, w: Int, xd: Float, yd: Float, zd: Float, wd: Float): Float {
    var hash = seed;
    hash = hash xor X_PRIME * x;
    hash = hash xor Y_PRIME * y;
    hash = hash xor Z_PRIME * z;
    hash = hash xor W_PRIME * w;

    hash = hash * hash * hash * 60493;
    hash = (hash shr 13) xor hash;

    hash = hash and 31;
    var a = yd
    var b = zd
    var c = wd;            // X,Y,Z
    when (hash shr 3) {          // OR, DEPENDING ON HIGH ORDER 2 BITS:
      1 -> {
        a = wd;
        b = xd;
        c = yd;
      }     // W,X,Y
      2 -> {
        a = zd;
        b = wd;
        c = xd;
      }
      // Z,W,X
      3 -> {
        a = yd;
        b = zd;
        c = wd;
        // Y,Z,W
      }
    }
    return (if ((hash and 4) == 0) -a else a) + (if ((hash and 2) == 0) -b else b) + (if ((hash and 1) == 0) -c else c)
  }

  fun GetNoise(x1: Float, y1: Float, z1: Float): Float {
    var x = x1 * m_frequency
    var y = y1 * m_frequency
    var z = z1 * m_frequency

    when (m_noiseType) {
      Value -> return SingleValue(m_seed, x, y, z)
      ValueFractal ->
        when(m_fractalType) {
          FBM -> return SingleValueFractalFBM(x, y, z);
          Billow ->  return SingleValueFractalBillow(x, y, z);
          RigidMulti -> return SingleValueFractalRigidMulti(x, y, z);
          else -> return 0f
        }
      Perlin -> return SinglePerlin(m_seed, x, y, z);
      PerlinFractal ->
        when (m_fractalType) {
          FBM -> return SinglePerlinFractalFBM(x, y, z);
          Billow -> return SinglePerlinFractalBillow(x, y, z);
          RigidMulti ->  return SinglePerlinFractalRigidMulti(x, y, z);
          else -> return 0f
        }
      Simplex -> return SingleSimplex(m_seed, x, y, z);
      SimplexFractal ->
        when (m_fractalType) {
          FBM -> return SingleSimplexFractalFBM(x, y, z);
          Billow -> return SingleSimplexFractalBillow(x, y, z);
          RigidMulti -> return SingleSimplexFractalRigidMulti(x, y, z);
          else -> return 0f
        }
      Cellular ->
        when (m_cellularReturnType) {
          CellValue,NoiseLookup,Distance-> return SingleCellular(x, y, z)
          else -> return SingleCellular2Edge(x, y, z);
        }
      WhiteNoise -> return GetWhiteNoise(x, y, z);
      Cubic -> return SingleCubic(m_seed, x, y, z);
      CubicFractal ->
        when (m_fractalType) {
          FBM -> return SingleCubicFractalFBM(x, y, z);
          Billow -> return SingleCubicFractalBillow(x, y, z);
          RigidMulti -> return SingleCubicFractalRigidMulti(x, y, z);
          else -> return 0f
        }
      else -> return 0f
    }
  }

  fun GetNoise(x1: Float, y1: Float): Float {
    var x = x1 * m_frequency;
    var y = y1 * m_frequency;

    when (m_noiseType) {
      Value -> return SingleValue(m_seed, x, y);
      ValueFractal ->
        when (m_fractalType) {
          FBM -> return SingleValueFractalFBM(x, y)
          Billow -> return SingleValueFractalBillow(x, y)
          RigidMulti -> return SingleValueFractalRigidMulti(x, y)
          else -> return 0f
        }
      Perlin -> return SinglePerlin(m_seed, x, y);
      PerlinFractal ->
        when (m_fractalType) {
          FBM -> return SinglePerlinFractalFBM(x, y);
          Billow -> return SinglePerlinFractalBillow(x, y);
          RigidMulti -> return SinglePerlinFractalRigidMulti(x, y);
          else -> return 0f
        }
      Simplex -> return SingleSimplex(m_seed, x, y);
      SimplexFractal ->
        when (m_fractalType) {
          FBM -> return SingleSimplexFractalFBM(x, y);
          Billow -> return SingleSimplexFractalBillow(x, y);
          RigidMulti -> return SingleSimplexFractalRigidMulti(x, y);
          else -> return 0f
        }
      Cellular ->
        when (m_cellularReturnType) {
          CellValue, NoiseLookup, Distance -> return SingleCellular(x, y)
          else -> return SingleCellular2Edge(x, y);
        }
      WhiteNoise -> return GetWhiteNoise(x, y);
      Cubic ->return SingleCubic(m_seed, x, y);
      CubicFractal ->
        when (m_fractalType) {
          FBM -> return SingleCubicFractalFBM(x, y);
          Billow -> return SingleCubicFractalBillow(x, y);
          RigidMulti -> return SingleCubicFractalRigidMulti(x, y);
          else -> return 0f
        }
      else -> return 0f;
    }
  }

  // White Noise

  private fun FloatCast2Int(f: Float): Int {
    val i = f.toRawBits()
    return i xor (i shr 16);
  }

  fun GetWhiteNoise(x: Float, y: Float, z: Float, w: Float): Float {
    val xi = FloatCast2Int(x);
    val yi = FloatCast2Int(y);
    val zi = FloatCast2Int(z);
    val wi = FloatCast2Int(w);

    return ValCoord4D(m_seed, xi, yi, zi, wi);
  }

  fun GetWhiteNoise(x: Float, y: Float, z: Float): Float {
    val xi = FloatCast2Int(x);
    val yi = FloatCast2Int(y);
    val zi = FloatCast2Int(z);

    return ValCoord3D(m_seed, xi, yi, zi);
  }

  fun GetWhiteNoise(x: Float, y: Float): Float {
    val xi = FloatCast2Int(x);
    val yi = FloatCast2Int(y);

    return ValCoord2D(m_seed, xi, yi);
  }

  fun GetWhiteNoiseInt(x: Int, y: Int, z: Int, w: Int): Float {
    return ValCoord4D(m_seed, x, y, z, w);
  }

  fun GetWhiteNoiseInt(x: Int, y: Int, z: Int): Float {
    return ValCoord3D(m_seed, x, y, z);
  }

  fun GetWhiteNoiseInt(x: Int, y: Int): Float {
    return ValCoord2D(m_seed, x, y);
  }

  // Value Noise
  fun GetValueFractal(x1: Float, y1: Float, z1: Float): Float {
    var x = x1 * m_frequency;
    var y = y1 * m_frequency;
    var z = z1 * m_frequency;

    when (m_fractalType) {
      FBM -> return SingleValueFractalFBM(x, y, z);
      Billow -> return SingleValueFractalBillow(x, y, z);
      RigidMulti -> return SingleValueFractalRigidMulti(x, y, z);
      else -> return 0f;
    }
  }

  fun SingleValueFractalFBM(x1: Float, y1: Float, z1: Float): Float {

    var x = x1
    var y = y1
    var z = z1

    var seed = m_seed;
    var sum = SingleValue(seed, x, y, z);
    var amp = 1f;

    for (i in 1 until m_octaves) {
      x *= m_lacunarity;
      y *= m_lacunarity;
      z *= m_lacunarity;

      amp *= m_gain;
      sum += SingleValue(++seed, x, y, z) * amp;
    }

    return sum * m_fractalBounding;
  }

  fun SingleValueFractalBillow(x1: Float, y1: Float, z1: Float): Float {

    var x = x1
    var y = y1
    var z = z1

    var seed = m_seed;
    var sum = Math.abs(SingleValue(seed, x, y, z)) * 2 - 1;
    var amp = 1f

    for (i in 1 until m_octaves) {
      x *= m_lacunarity;
      y *= m_lacunarity;
      z *= m_lacunarity;

      amp *= m_gain;
      sum += (Math.abs(SingleValue(++seed, x, y, z)) * 2 - 1) * amp;
    }

    return sum * m_fractalBounding;
  }

  fun SingleValueFractalRigidMulti(x1: Float, y1: Float, z1: Float): Float {

    var x = x1
    var y = y1
    var z = z1

    var seed = m_seed;
    var sum = 1 - Math.abs(SingleValue(seed, x, y, z));
    var amp = 1f;

    for (i in 1 until m_octaves) {
      x *= m_lacunarity;
      y *= m_lacunarity;
      z *= m_lacunarity;

      amp *= m_gain;
      sum -= (1 - Math.abs(SingleValue(++seed, x, y, z))) * amp;
    }

    return sum;
  }

  fun GetValue(x: Float, y: Float, z: Float): Float {
    return SingleValue(m_seed, x * m_frequency, y * m_frequency, z * m_frequency);
  }

  fun SingleValue(seed: Int, x: Float, y: Float, z: Float): Float {
    var x0 = FastFloor(x);
    var y0 = FastFloor(y);
    var z0 = FastFloor(z);
    var x1 = x0 + 1;
    var y1 = y0 + 1;
    var z1 = z0 + 1;

    var xs = 0f
    var ys = 0f
    var zs = 0f

    when (m_interp) {
      Hermite -> {
        xs = InterpHermiteFunc(x - x0);
        ys = InterpHermiteFunc(y - y0);
        zs = InterpHermiteFunc(z - z0);
      }
      Quintic -> {
        xs = InterpQuinticFunc(x - x0);
        ys = InterpQuinticFunc(y - y0);
        zs = InterpQuinticFunc(z - z0);
      }
      else -> {
        xs = x - x0;
        ys = y - y0;
        zs = z - z0;
      }
    }

    var xf00 = Lerp(ValCoord3D(seed, x0, y0, z0), ValCoord3D(seed, x1, y0, z0), xs);
    var xf10 = Lerp(ValCoord3D(seed, x0, y1, z0), ValCoord3D(seed, x1, y1, z0), xs);
    var xf01 = Lerp(ValCoord3D(seed, x0, y0, z1), ValCoord3D(seed, x1, y0, z1), xs);
    var xf11 = Lerp(ValCoord3D(seed, x0, y1, z1), ValCoord3D(seed, x1, y1, z1), xs);

    var yf0 = Lerp(xf00, xf10, ys);
    var yf1 = Lerp(xf01, xf11, ys);

    return Lerp(yf0, yf1, zs);
  }

  fun GetValueFractal(x1: Float, y1: Float): Float {

    var x = x1
    var y = y1

    x *= m_frequency;
    y *= m_frequency;

    when (m_fractalType) {
      FBM -> return SingleValueFractalFBM(x, y);
      Billow -> return SingleValueFractalBillow(x, y);
      RigidMulti -> return SingleValueFractalRigidMulti(x, y);
      else -> return 0f
    }
  }

  fun SingleValueFractalFBM(x1: Float, y1: Float): Float {

    var x = x1
    var y = y1

    var seed = m_seed;
    var sum = SingleValue(seed, x, y);
    var amp = 1f

    for (i in 1 until m_octaves) {
      x *= m_lacunarity;
      y *= m_lacunarity;

      amp *= m_gain;
      sum += SingleValue(++seed, x, y) * amp;
    }

    return sum * m_fractalBounding;
  }

  fun SingleValueFractalBillow(x1: Float, y1: Float): Float {

    var x = x1
    var y = y1

    var seed = m_seed;
    var sum = Math.abs(SingleValue(seed, x, y)) * 2 - 1;
    var amp = 1f

    for (i in 1 until m_octaves) {
      x *= m_lacunarity;
      y *= m_lacunarity;
      amp *= m_gain;
      sum += (Math.abs(SingleValue(++seed, x, y)) * 2 - 1) * amp;
    }

    return sum * m_fractalBounding;
  }

  fun SingleValueFractalRigidMulti(x1: Float, y1: Float): Float {

    var x = x1
    var y = y1

    var seed = m_seed;
    var sum = 1 - Math.abs(SingleValue(seed, x, y));
    var amp = 1f

    for (i in 1 until m_octaves) {
      x *= m_lacunarity;
      y *= m_lacunarity;

      amp *= m_gain;
      sum -= (1 - Math.abs(SingleValue(++seed, x, y))) * amp;
    }

    return sum;
  }

  fun GetValue(x: Float, y: Float): Float {
    return SingleValue(m_seed, x * m_frequency, y * m_frequency);
  }

  fun SingleValue(seed: Int, x: Float, y: Float): Float {
    var x0 = FastFloor(x);
    var y0 = FastFloor(y);
    var x1 = x0 + 1;
    var y1 = y0 + 1;

    var xs = 0f
    var ys = 0f

    when (m_interp) {
      Hermite -> {
        xs = InterpHermiteFunc(x - x0);
        ys = InterpHermiteFunc(y - y0);
      }
      Quintic -> {
        xs = InterpQuinticFunc(x - x0);
        ys = InterpQuinticFunc(y - y0);
      }
      else -> {
        xs = x - x0;
        ys = y - y0;
      }
    }

    var xf0 = Lerp(ValCoord2D(seed, x0, y0), ValCoord2D(seed, x1, y0), xs);
    var xf1 = Lerp(ValCoord2D(seed, x0, y1), ValCoord2D(seed, x1, y1), xs);

    return Lerp(xf0, xf1, ys);
  }

  // Gradient Noise
  fun GetPerlinFractal(x1: Float, y1: Float, z1: Float): Float {

    var x = x1
    var y = y1
    var z = z1

    x *= m_frequency;
    y *= m_frequency;
    z *= m_frequency;

    when (m_fractalType) {
      FBM -> return SinglePerlinFractalFBM(x, y, z);
      Billow -> return SinglePerlinFractalBillow(x, y, z);
      RigidMulti -> return SinglePerlinFractalRigidMulti(x, y, z);
      else -> return 0f
    }
  }

  fun SinglePerlinFractalFBM(x1: Float, y1: Float, z1: Float): Float {

    var x = x1
    var y = y1
    var z = z1

    var seed = m_seed;
    var sum = SinglePerlin(seed, x, y, z);
    var amp = 1f

    for (i in 1 until m_octaves) {
      x *= m_lacunarity;
      y *= m_lacunarity;
      z *= m_lacunarity;

      amp *= m_gain;
      sum += SinglePerlin(++seed, x, y, z) * amp;
    }

    return sum * m_fractalBounding;
  }

  fun SinglePerlinFractalBillow(x1: Float, y1: Float, z1: Float): Float {

    var x = x1
    var y = y1
    var z = z1

    var seed = m_seed;
    var sum = Math.abs(SinglePerlin(seed, x, y, z)) * 2 - 1;
    var amp = 1f

    for (i in 1 until m_octaves) {
      x *= m_lacunarity;
      y *= m_lacunarity;
      z *= m_lacunarity;

      amp *= m_gain;
      sum += (Math.abs(SinglePerlin(++seed, x, y, z)) * 2 - 1) * amp;
    }

    return sum * m_fractalBounding;
  }

  fun SinglePerlinFractalRigidMulti(x1: Float, y1: Float, z1: Float): Float {

    var x = x1
    var y = y1
    var z = z1

    var seed = m_seed;
    var sum = 1 - Math.abs(SinglePerlin(seed, x, y, z));
    var amp = 1;

    for (i in 1 until m_octaves) {
      x *= m_lacunarity;
      y *= m_lacunarity;
      z *= m_lacunarity;

      amp *= m_gain;
      sum -= (1 - Math.abs(SinglePerlin(++seed, x, y, z))) * amp;
    }

    return sum;
  }

  fun GetPerlin(x: Float, y: Float, z: Float): Float {
    return SinglePerlin(m_seed, x * m_frequency, y * m_frequency, z * m_frequency);
  }

  fun SinglePerlin(seed: Int, x: Float, y: Float, z: Float): Float {

    var x0 = FastFloor(x);
    var y0 = FastFloor(y);
    var z0 = FastFloor(z);
    var x1 = x0 + 1;
    var y1 = y0 + 1;
    var z1 = z0 + 1;

    var xs = 0f
    var ys = 0f
    var zs = 0f
    when (m_interp) {
      Hermite -> {
        xs = InterpHermiteFunc(x - x0);
        ys = InterpHermiteFunc(y - y0);
        zs = InterpHermiteFunc(z - z0);
      }
      Quintic -> {
        xs = InterpQuinticFunc(x - x0);
        ys = InterpQuinticFunc(y - y0);
        zs = InterpQuinticFunc(z - z0);
      }
      else -> {
        xs = x - x0;
        ys = y - y0;
        zs = z - z0;
      }
    }

    var xd0 = x - x0;
    var yd0 = y - y0;
    var zd0 = z - z0;
    var xd1 = xd0 - 1;
    var yd1 = yd0 - 1;
    var zd1 = zd0 - 1;

    var xf00 = Lerp(GradCoord3D(seed, x0, y0, z0, xd0, yd0, zd0), GradCoord3D(seed, x1, y0, z0, xd1, yd0, zd0), xs);
    var xf10 = Lerp(GradCoord3D(seed, x0, y1, z0, xd0, yd1, zd0), GradCoord3D(seed, x1, y1, z0, xd1, yd1, zd0), xs);
    var xf01 = Lerp(GradCoord3D(seed, x0, y0, z1, xd0, yd0, zd1), GradCoord3D(seed, x1, y0, z1, xd1, yd0, zd1), xs);
    var xf11 = Lerp(GradCoord3D(seed, x0, y1, z1, xd0, yd1, zd1), GradCoord3D(seed, x1, y1, z1, xd1, yd1, zd1), xs);

    var yf0 = Lerp(xf00, xf10, ys);
    var yf1 = Lerp(xf01, xf11, ys);

    return Lerp(yf0, yf1, zs);
  }

  fun GetPerlinFractal(x1: Float, y1: Float): Float {

    var x = x1
    var y = y1

    x *= m_frequency;
    y *= m_frequency;

    when (m_fractalType) {
      FBM -> return SinglePerlinFractalFBM(x, y);
      Billow -> return SinglePerlinFractalBillow(x, y);
      RigidMulti -> return SinglePerlinFractalRigidMulti(x, y);
      else -> return 0f
    }
  }

  fun SinglePerlinFractalFBM(x1: Float, y1: Float): Float {

    var x = x1
    var y = y1

    var seed = m_seed;
    var sum = SinglePerlin(seed, x, y);
    var amp = 1f

    for (i in 1 until m_octaves) {
      x *= m_lacunarity;
      y *= m_lacunarity;

      amp *= m_gain;
      sum += SinglePerlin(++seed, x, y) * amp;
    }

    return sum * m_fractalBounding;
  }

  fun SinglePerlinFractalBillow(x1: Float, y1: Float): Float {

    var x = x1
    var y = y1

    var seed = m_seed;
    var sum = Math.abs(SinglePerlin(seed, x, y)) * 2 - 1;
    var amp = 1f

    for (i in 1 until m_octaves) {
      x *= m_lacunarity;
      y *= m_lacunarity;

      amp *= m_gain;
      sum += (Math.abs(SinglePerlin(++seed, x, y)) * 2 - 1) * amp;
    }

    return sum * m_fractalBounding;
  }

  fun SinglePerlinFractalRigidMulti(x1: Float, y1: Float): Float {

    var x = x1
    var y = y1

    var seed = m_seed;
    var sum = 1 - Math.abs(SinglePerlin(seed, x, y));
    var amp = 1f

    for (i in 1 until m_octaves) {
      x *= m_lacunarity;
      y *= m_lacunarity;

      amp *= m_gain;
      sum -= (1 - Math.abs(SinglePerlin(++seed, x, y))) * amp;
    }

    return sum;
  }

  fun GetPerlin(x: Float, y: Float): Float {
    return SinglePerlin(m_seed, x * m_frequency, y * m_frequency);
  }

  fun SinglePerlin(seed: Int, x: Float, y: Float): Float {
    var x0 = FastFloor(x);
    var y0 = FastFloor(y);
    var x1 = x0 + 1;
    var y1 = y0 + 1;

    var xs = 0f
    var ys = 0f
    when (m_interp) {
      Hermite -> {
        xs = InterpHermiteFunc(x - x0);
        ys = InterpHermiteFunc(y - y0);
      }
      Quintic -> {
        xs = InterpQuinticFunc(x - x0);
        ys = InterpQuinticFunc(y - y0);
      }
      else -> {
        xs = x - x0;
        ys = y - y0;
      }
    }

    var xd0 = x - x0;
    var yd0 = y - y0;
    var xd1 = xd0 - 1;
    var yd1 = yd0 - 1;

    var xf0 = Lerp(GradCoord2D(seed, x0, y0, xd0, yd0), GradCoord2D(seed, x1, y0, xd1, yd0), xs);
    var xf1 = Lerp(GradCoord2D(seed, x0, y1, xd0, yd1), GradCoord2D(seed, x1, y1, xd1, yd1), xs);

    return Lerp(xf0, xf1, ys);
  }

  // Simplex Noise
  fun GetSimplexFractal(x1: Float, y1: Float, z1: Float): Float {

    var x = x1
    var y = y1
    var z = z1

    x *= m_frequency;
    y *= m_frequency;
    z *= m_frequency;

    when (m_fractalType) {
      FBM -> return SingleSimplexFractalFBM(x, y, z);
      Billow -> SingleSimplexFractalBillow(x, y, z);
      RigidMulti -> return SingleSimplexFractalRigidMulti(x, y, z);
      else -> return 0f
    }
  }

  fun SingleSimplexFractalFBM(x1: Float, y1: Float, z1: Float): Float {

    var x = x1
    var y = y1
    var z = z1

    var seed = m_seed;
    var sum = SingleSimplex(seed, x, y, z);
    var amp = 1;

    for (i in 1 until m_octaves) {
      x *= m_lacunarity;
      y *= m_lacunarity;
      z *= m_lacunarity;

      amp *= m_gain;
      sum += SingleSimplex(++seed, x, y, z) * amp;
    }

    return sum * m_fractalBounding;
  }

  fun SingleSimplexFractalBillow(x1: Float, y1: Float, z1: Float): Float {

    var x = x1
    var y = y1
    var z = z1

    var seed = m_seed;
    var sum = Math.abs(SingleSimplex(seed, x, y, z)) * 2 - 1;
    var amp = 1;

    for (i in 1 until m_octaves) {
      x *= m_lacunarity;
      y *= m_lacunarity;
      z *= m_lacunarity;

      amp *= m_gain;
      sum += (Math.abs(SingleSimplex(++seed, x, y, z)) * 2 - 1) * amp;
    }

    return sum * m_fractalBounding;
  }

  fun SingleSimplexFractalRigidMulti(x1: Float, y1: Float, z1: Float): Float {

    var x = x1
    var y = y1
    var z = z1

    var seed = m_seed;
    var sum = 1 - Math.abs(SingleSimplex(seed, x, y, z));
    var amp = 1;

    for (i in 1 until m_octaves) {
      x *= m_lacunarity;
      y *= m_lacunarity;
      z *= m_lacunarity;

      amp *= m_gain;
      sum -= (1 - Math.abs(SingleSimplex(++seed, x, y, z))) * amp;
    }

    return sum;
  }

  fun GetSimplex(x: Float, y: Float, z: Float): Float {
    return SingleSimplex(m_seed, x * m_frequency, y * m_frequency, z * m_frequency);
  }

  private val F3 = (1.0f / 3.0f);
  private val G3 = (1.0f / 6.0f);
  private val G33 = G3 * 3f - 1f;

  fun SingleSimplex(seed: Int, x: Float, y: Float, z: Float): Float {
    var t = (x + y + z) * F3;
    var i: Int = FastFloor(x + t);
    var j: Int = FastFloor(y + t);
    var k: Int = FastFloor(z + t);

    t = (i + j + k) * G3;
    var x0 = x - (i - t);
    var y0 = y - (j - t);
    var z0 = z - (k - t);

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

    var x1 = x0 - i1 + G3;
    var y1 = y0 - j1 + G3;
    var z1 = z0 - k1 + G3;
    var x2 = x0 - i2 + F3;
    var y2 = y0 - j2 + F3;
    var z2 = z0 - k2 + F3;
    var x3 = x0 + G33;
    var y3 = y0 + G33;
    var z3 = z0 + G33;

    var n0 = 0f
    var n1 = 0f
    var n2 = 0f
    var n3 = 0f

    t = 0.6f - x0 * x0 - y0 * y0 - z0 * z0;
    if (t < 0) n0 = 0f
    else {
      t *= t;
      n0 = t * t * GradCoord3D(seed, i, j, k, x0, y0, z0);
    }

    t = 0.6f - x1 * x1 - y1 * y1 - z1 * z1;
    if (t < 0) n1 = 0f
    else {
      t *= t;
      n1 = t * t * GradCoord3D(seed, i + i1, j + j1, k + k1, x1, y1, z1);
    }

    t = 0.6f - x2 * x2 - y2 * y2 - z2 * z2;
    if (t < 0) n2 = 0f
    else {
      t *= t;
      n2 = t * t * GradCoord3D(seed, i + i2, j + j2, k + k2, x2, y2, z2);
    }

    t = 0.6f - x3 * x3 - y3 * y3 - z3 * z3;
    if (t < 0) n3 = 0f
    else {
      t *= t;
      n3 = t * t * GradCoord3D(seed, i + 1, j + 1, k + 1, x3, y3, z3);
    }

    return 32 * (n0 + n1 + n2 + n3);
  }

  fun GetSimplexFractal(x1: Float, y1: Float): Float {

    var x = x1
    var y = y1

    x *= m_frequency;
    y *= m_frequency;

    when (m_fractalType) {
      FBM -> return SingleSimplexFractalFBM(x, y);
      Billow -> return SingleSimplexFractalBillow(x, y);
      RigidMulti -> return SingleSimplexFractalRigidMulti(x, y);
      else -> return 0f
    }
  }

  fun SingleSimplexFractalFBM(x1: Float, y1: Float): Float {

    var x = x1
    var y = y1

    var seed = m_seed;
    var sum = SingleSimplex(seed, x, y);
    var amp = 1f

    for (i in 1 until m_octaves) {
      x *= m_lacunarity;
      y *= m_lacunarity;

      amp *= m_gain;
      sum += SingleSimplex(++seed, x, y) * amp;
    }

    return sum * m_fractalBounding;
  }

  fun SingleSimplexFractalBillow(x1: Float, y1: Float): Float {

    var x = x1
    var y = y1

    var seed = m_seed;
    var sum = Math.abs(SingleSimplex(seed, x, y)) * 2 - 1;
    var amp = 1f

    for (i in 1 until m_octaves) {
      x *= m_lacunarity;
      y *= m_lacunarity;

      amp *= m_gain;
      sum += (Math.abs(SingleSimplex(++seed, x, y)) * 2 - 1) * amp;
    }

    return sum * m_fractalBounding;
  }

  fun SingleSimplexFractalRigidMulti(x: Float, y: Float): Float {
    var seed = m_seed;
    var sum = 1 - Math.abs(SingleSimplex(seed, x, y));
    var amp = 1;

    for (i in 1 until m_octaves) {
      x *= m_lacunarity;
      y *= m_lacunarity;

      amp *= m_gain;
      sum -= (1 - Math.abs(SingleSimplex(++seed, x, y))) * amp;
    }

    return sum;
  }

  fun GetSimplex(x: Float, y: Float): Float {
    return SingleSimplex(m_seed, x * m_frequency, y * m_frequency);
  }

  private val SQRT3 = 1.7320508075688772935274463415059f
  private val F2 = 0.5f * (SQRT3 - 1.0f);
  private val G2 = (3.0f - SQRT3) / 6.0f;

  fun SingleSimplex(seed: Int, x: Float, y: Float): Float {
    var t = (x + y) * F2;
    var i = FastFloor(x + t);
    var j = FastFloor(y + t);

    t = (i + j) * G2;
    var X0 = i - t;
    var Y0 = j - t;

    var x0 = x - X0;
    var y0 = y - Y0;

    var i1 = 0
    var j1 = 0

    if (x0 > y0) {
      i1 = 1;
      j1 = 0;
    } else {
      i1 = 0;
      j1 = 1;
    }

    var x1 = x0 - i1 + G2;
    var y1 = y0 - j1 + G2;
    var x2 = x0 - 1 + 2*G2;
    var y2 = y0 - 1 + 2*G2;

    var n0 = 0f
    var n1 = 0f
    var n2 = 0f

    t = 0.5f - x0 * x0 - y0 * y0;
    if (t < 0) n0 = 0f
    else {
      t *= t;
      n0 = t * t * GradCoord2D(seed, i, j, x0, y0);
    }

    t = 0.5f - x1 * x1 - y1 * y1;
    if (t < 0) n1 = 0f
    else {
      t *= t;
      n1 = t * t * GradCoord2D(seed, i + i1, j + j1, x1, y1);
    }

    t = 0.5f - x2 * x2 - y2 * y2;
    if (t < 0) n2 = 0f
    else {
      t *= t;
      n2 = t * t * GradCoord2D(seed, i + 1, j + 1, x2, y2);
    }

    return 50 * (n0 + n1 + n2);
  }

  fun GetSimplex(x: Float, y: Float, z: Float, w: Float): Float {
    return SingleSimplex(m_seed, x * m_frequency, y * m_frequency, z * m_frequency, w * m_frequency);
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

  private val F4 = ((2.23606797f - 1.0f) / 4.0f);
  private val G4 = ((5.0f - 2.23606797f) / 20.0f);

  fun SingleSimplex(seed: Int, x: Float, y: Float, z: Float, w: Float): Float {
    var n0 = 0f
    var n1 = 0f
    var n2 = 0f
    var n3 = 0f
    var n4 = 0f

    var t = (x + y + z + w) * F4;
    var i = FastFloor(x + t);
    var j = FastFloor(y + t);
    var k = FastFloor(z + t);
    var l = FastFloor(w + t);
    t = (i + j + k + l) * G4;
    var X0 = i - t;
    var Y0 = j - t;
    var Z0 = k - t;
    var W0 = l - t;
    var x0 = x - X0;
    var y0 = y - Y0;
    var z0 = z - Z0;
    var w0 = w - W0;

    var c = if (x0 > y0) 32 else 0;
    c += if (x0 > z0) 16 else 0;
    c += if (y0 > z0) 8 else 0;
    c += if (x0 > w0) 4 else 0;
    c += if (y0 > w0) 2 else 0;
    c += if (z0 > w0) 1 else 0;
    c = c shl 2

    var i1 = if (SIMPLEX_4D[c] >= 3) 1 else 0;
    var i2 = if (SIMPLEX_4D[c] >= 2) 1 else 0;
    var i3 = if (SIMPLEX_4D[c++] >= 1) 1 else 0;
    var j1 = if (SIMPLEX_4D[c] >= 3) 1 else 0;
    var j2 = if (SIMPLEX_4D[c] >= 2) 1 else 0;
    var j3 = if (SIMPLEX_4D[c++] >= 1) 1 else 0;
    var k1 = if (SIMPLEX_4D[c] >= 3) 1 else 0;
    var k2 = if (SIMPLEX_4D[c] >= 2) 1 else 0;
    var k3 = if (SIMPLEX_4D[c++] >= 1) 1 else 0;
    var l1 = if (SIMPLEX_4D[c] >= 3) 1 else 0;
    var l2 = if (SIMPLEX_4D[c] >= 2) 1 else 0;
    var l3 = if (SIMPLEX_4D[c] >= 1) 1 else 0;

    var x1 = x0 - i1 + G4;
    var y1 = y0 - j1 + G4;
    var z1 = z0 - k1 + G4;
    var w1 = w0 - l1 + G4;
    var x2 = x0 - i2 + 2 * G4;
    var y2 = y0 - j2 + 2 * G4;
    var z2 = z0 - k2 + 2 * G4;
    var w2 = w0 - l2 + 2 * G4;
    var x3 = x0 - i3 + 3 * G4;
    var y3 = y0 - j3 + 3 * G4;
    var z3 = z0 - k3 + 3 * G4;
    var w3 = w0 - l3 + 3 * G4;
    var x4 = x0 - 1 + 4 * G4;
    var y4 = y0 - 1 + 4 * G4;
    var z4 = z0 - 1 + 4 * G4;
    var w4 = w0 - 1 + 4 * G4;

    t = 0.6f - x0 * x0 - y0 * y0 - z0 * z0 - w0 * w0;
    if (t < 0) n0 = 0f
    else {
      t *= t;
      n0 = t * t * GradCoord4D(seed, i, j, k, l, x0, y0, z0, w0);
    }
    t = 0.6f - x1 * x1 - y1 * y1 - z1 * z1 - w1 * w1;
    if (t < 0) n1 = 0f
    else {
      t *= t;
      n1 = t * t * GradCoord4D(seed, i + i1, j + j1, k + k1, l + l1, x1, y1, z1, w1);
    }
    t = 0.6f - x2 * x2 - y2 * y2 - z2 * z2 - w2 * w2;
    if (t < 0) n2 = 0f
    else {
      t *= t;
      n2 = t * t * GradCoord4D(seed, i + i2, j + j2, k + k2, l + l2, x2, y2, z2, w2);
    }
    t = 0.6f - x3 * x3 - y3 * y3 - z3 * z3 - w3 * w3;
    if (t < 0) n3 = 0f
    else {
      t *= t;
      n3 = t * t * GradCoord4D(seed, i + i3, j + j3, k + k3, l + l3, x3, y3, z3, w3);
    }
    t = 0.6f - x4 * x4 - y4 * y4 - z4 * z4 - w4 * w4;
    if (t < 0) n4 = 0f
    else {
      t *= t;
      n4 = t * t * GradCoord4D(seed, i + 1, j + 1, k + 1, l + 1, x4, y4, z4, w4);
    }

    return 27 * (n0 + n1 + n2 + n3 + n4);
  }

  // Cubic Noise
  fun GetCubicFractal(x1: Float, y1: Float, z1: Float): Float {

    var x = x1
    var y = y1
    var z = z1

    x *= m_frequency;
    y *= m_frequency;
    z *= m_frequency;

    when (m_fractalType) {
      FBM -> return SingleCubicFractalFBM(x, y, z);
      Billow -> return SingleCubicFractalBillow(x, y, z);
      RigidMulti -> return SingleCubicFractalRigidMulti(x, y, z);
      else -> return 0f
    }
  }

  fun SingleCubicFractalFBM(x1: Float, y1: Float, z1: Float): Float {

    var x = x1
    var y = y1
    var z = z1

    var seed = m_seed;
    var sum = SingleCubic(seed, x, y, z);
    var amp = 1f
    var i = 0;

    while (++i < m_octaves) {
      x *= m_lacunarity;
      y *= m_lacunarity;
      z *= m_lacunarity;

      amp *= m_gain;
      sum += SingleCubic(++seed, x, y, z) * amp;
    }

    return sum * m_fractalBounding;
  }

  fun SingleCubicFractalBillow(x1: Float, y1: Float, z1: Float): Float {

    var x = x1
    var y = y1
    var z = z1

    var seed = m_seed;
    var sum = Math.abs(SingleCubic(seed, x, y, z)) * 2 - 1;
    var amp = 1;
    var i = 0;

    while (++i < m_octaves) {
      x *= m_lacunarity;
      y *= m_lacunarity;
      z *= m_lacunarity;

      amp *= m_gain;
      sum += (Math.abs(SingleCubic(++seed, x, y, z)) * 2 - 1) * amp;
    }

    return sum * m_fractalBounding;
  }

  fun SingleCubicFractalRigidMulti(x1: Float, y1: Float, z1: Float): Float {

    var x = x1
    var y = y1
    var z = z1

    var seed = m_seed;
    var sum = 1 - Math.abs(SingleCubic(seed, x, y, z));
    var amp = 1f
    var i = 0;

    while (++i < m_octaves) {
      x *= m_lacunarity;
      y *= m_lacunarity;
      z *= m_lacunarity;

      amp *= m_gain;
      sum -= (1 - Math.abs(SingleCubic(++seed, x, y, z))) * amp;
    }

    return sum;
  }

  fun GetCubic(x: Float, y: Float, z: Float): Float {
    return SingleCubic(m_seed, x * m_frequency, y * m_frequency, z * m_frequency);
  }

  private val CUBIC_3D_BOUNDING = 1f / (1.5f * 1.5f * 1.5f);

  fun SingleCubic(seed: Int, x: Float, y: Float, z: Float): Float {
    var x1 = FastFloor(x);
    var y1 = FastFloor(y);
    var z1 = FastFloor(z);

    var x0 = x1 - 1;
    var y0 = y1 - 1;
    var z0 = z1 - 1;
    var x2 = x1 + 1;
    var y2 = y1 + 1;
    var z2 = z1 + 1;
    var x3 = x1 + 2;
    var y3 = y1 + 2;
    var z3 = z1 + 2;

    var xs = x - x1.toFloat()
    var ys = y - y1.toFloat()
    var zs = z - z1.toFloat()

    return CubicLerp(
      CubicLerp(
        CubicLerp(ValCoord3D(seed, x0, y0, z0), ValCoord3D(seed, x1, y0, z0), ValCoord3D(seed, x2, y0, z0), ValCoord3D(seed, x3, y0, z0), xs),
        CubicLerp(ValCoord3D(seed, x0, y1, z0), ValCoord3D(seed, x1, y1, z0), ValCoord3D(seed, x2, y1, z0), ValCoord3D(seed, x3, y1, z0), xs),
        CubicLerp(ValCoord3D(seed, x0, y2, z0), ValCoord3D(seed, x1, y2, z0), ValCoord3D(seed, x2, y2, z0), ValCoord3D(seed, x3, y2, z0), xs),
        CubicLerp(ValCoord3D(seed, x0, y3, z0), ValCoord3D(seed, x1, y3, z0), ValCoord3D(seed, x2, y3, z0), ValCoord3D(seed, x3, y3, z0), xs),
        ys),
      CubicLerp(
        CubicLerp(ValCoord3D(seed, x0, y0, z1), ValCoord3D(seed, x1, y0, z1), ValCoord3D(seed, x2, y0, z1), ValCoord3D(seed, x3, y0, z1), xs),
        CubicLerp(ValCoord3D(seed, x0, y1, z1), ValCoord3D(seed, x1, y1, z1), ValCoord3D(seed, x2, y1, z1), ValCoord3D(seed, x3, y1, z1), xs),
        CubicLerp(ValCoord3D(seed, x0, y2, z1), ValCoord3D(seed, x1, y2, z1), ValCoord3D(seed, x2, y2, z1), ValCoord3D(seed, x3, y2, z1), xs),
        CubicLerp(ValCoord3D(seed, x0, y3, z1), ValCoord3D(seed, x1, y3, z1), ValCoord3D(seed, x2, y3, z1), ValCoord3D(seed, x3, y3, z1), xs),
        ys),
      CubicLerp(
        CubicLerp(ValCoord3D(seed, x0, y0, z2), ValCoord3D(seed, x1, y0, z2), ValCoord3D(seed, x2, y0, z2), ValCoord3D(seed, x3, y0, z2), xs),
        CubicLerp(ValCoord3D(seed, x0, y1, z2), ValCoord3D(seed, x1, y1, z2), ValCoord3D(seed, x2, y1, z2), ValCoord3D(seed, x3, y1, z2), xs),
        CubicLerp(ValCoord3D(seed, x0, y2, z2), ValCoord3D(seed, x1, y2, z2), ValCoord3D(seed, x2, y2, z2), ValCoord3D(seed, x3, y2, z2), xs),
        CubicLerp(ValCoord3D(seed, x0, y3, z2), ValCoord3D(seed, x1, y3, z2), ValCoord3D(seed, x2, y3, z2), ValCoord3D(seed, x3, y3, z2), xs),
        ys),
      CubicLerp(
        CubicLerp(ValCoord3D(seed, x0, y0, z3), ValCoord3D(seed, x1, y0, z3), ValCoord3D(seed, x2, y0, z3), ValCoord3D(seed, x3, y0, z3), xs),
        CubicLerp(ValCoord3D(seed, x0, y1, z3), ValCoord3D(seed, x1, y1, z3), ValCoord3D(seed, x2, y1, z3), ValCoord3D(seed, x3, y1, z3), xs),
        CubicLerp(ValCoord3D(seed, x0, y2, z3), ValCoord3D(seed, x1, y2, z3), ValCoord3D(seed, x2, y2, z3), ValCoord3D(seed, x3, y2, z3), xs),
        CubicLerp(ValCoord3D(seed, x0, y3, z3), ValCoord3D(seed, x1, y3, z3), ValCoord3D(seed, x2, y3, z3), ValCoord3D(seed, x3, y3, z3), xs),
        ys),
      zs) * CUBIC_3D_BOUNDING;
  }


  fun GetCubicFractal(x1: Float, y1: Float): Float {

    var x = x1
    var y = y1

    x *= m_frequency;
    y *= m_frequency;

    when (m_fractalType) {
      FBM -> return SingleCubicFractalFBM(x, y);
      Billow -> SingleCubicFractalBillow(x, y);
      RigidMulti -> return SingleCubicFractalRigidMulti(x, y);
      else -> return 0f
    }
  }

  fun SingleCubicFractalFBM(x1: Float, y1: Float): Float {

    var x = x1
    var y = y1

    var seed = m_seed;
    var sum = SingleCubic(seed, x, y);
    var amp = 1f
    var i = 0;

    while (++i < m_octaves) {
      x *= m_lacunarity;
      y *= m_lacunarity;

      amp *= m_gain;
      sum += SingleCubic(++seed, x, y) * amp;
    }

    return sum * m_fractalBounding;
  }

  fun SingleCubicFractalBillow(x1: Float, y1: Float): Float {

    var x = x1
    var y = y1

    var seed = m_seed;
    var sum = Math.abs(SingleCubic(seed, x, y)) * 2 - 1;
    var amp = 1f
    var i = 0;

    while (++i < m_octaves) {
      x *= m_lacunarity;
      y *= m_lacunarity;

      amp *= m_gain;
      sum += (Math.abs(SingleCubic(++seed, x, y)) * 2 - 1) * amp;
    }

    return sum * m_fractalBounding;
  }

  fun SingleCubicFractalRigidMulti(x1: Float, y1: Float): Float {

    var x = x1
    var y = y1

    var seed = m_seed;
    var sum = 1 - Math.abs(SingleCubic(seed, x, y));
    var amp = 1f
    var i = 0;

    while (++i < m_octaves) {
      x *= m_lacunarity;
      y *= m_lacunarity;

      amp *= m_gain;
      sum -= (1 - Math.abs(SingleCubic(++seed, x, y))) * amp;
    }

    return sum;
  }

  fun GetCubic(x1: Float, y1: Float): Float {

    var x = x1
    var y = y1

    x *= m_frequency;
    y *= m_frequency;

    return SingleCubic(0, x, y);
  }

  private val CUBIC_2D_BOUNDING = 1f / (1.5f * 1.5f);

  private fun SingleCubic(seed: Int, x: Float, y: Float): Float {
    var x1 = FastFloor(x);
    var y1 = FastFloor(y);

    var x0 = x1 - 1;
    var y0 = y1 - 1;
    var x2 = x1 + 1;
    var y2 = y1 + 1;
    var x3 = x1 + 2;
    var y3 = y1 + 2;

    var xs = x - x1.toFloat();
    var ys = y - y1.toFloat();

    return CubicLerp(
      CubicLerp(ValCoord2D(seed, x0, y0), ValCoord2D(seed, x1, y0), ValCoord2D(seed, x2, y0), ValCoord2D(seed, x3, y0),
        xs),
      CubicLerp(ValCoord2D(seed, x0, y1), ValCoord2D(seed, x1, y1), ValCoord2D(seed, x2, y1), ValCoord2D(seed, x3, y1),
        xs),
      CubicLerp(ValCoord2D(seed, x0, y2), ValCoord2D(seed, x1, y2), ValCoord2D(seed, x2, y2), ValCoord2D(seed, x3, y2),
        xs),
      CubicLerp(ValCoord2D(seed, x0, y3), ValCoord2D(seed, x1, y3), ValCoord2D(seed, x2, y3), ValCoord2D(seed, x3, y3),
        xs),
      ys) * CUBIC_2D_BOUNDING;
  }

  // Cellular Noise
  fun GetCellular(x1: Float, y1: Float, z1: Float): Float {

    var x = x1
    var y = y1
    var z = z1

    x *= m_frequency;
    y *= m_frequency;
    z *= m_frequency;

    when (m_cellularReturnType) {
      CellValue,NoiseLookup,Distance -> return SingleCellular(x, y, z);
      else -> return SingleCellular2Edge(x, y, z);
    }
  }

  fun SingleCellular(x: Float, y: Float, z: Float): Float {
    var xr = FastRound(x);
    var yr = FastRound(y);
    var zr = FastRound(z);

    var distance = 999999f;
    var xc = 0
    var yc = 0
    var zc = 0;

    when (m_cellularDistanceFunction) {
      Euclidean -> {
        for (xi in (xr - 1) .. (xr + 1)) {
          for (yi in (yr - 1) ..  (yr + 1)) {
            for (zi in (zr - 1) .. (zr + 1)) {
              var vec = CELL_3D [Hash3D(m_seed, xi, yi, zi) and 255];

              var vecX = xi -x + vec.x;
              var vecY = yi -y + vec.y;
              var vecZ = zi -z + vec.z;

              var newDistance = vecX * vecX + vecY * vecY + vecZ * vecZ;

              if (newDistance < distance) {
                distance = newDistance;
                xc = xi;
                yc = yi;
                zc = zi;
              }
            }
          }
        }
      }
      Manhattan -> {
        for (xi in (xr - 1) .. (xr + 1)) {
          for (yi in (yr - 1) .. (yr + 1)) {
            for (zi in (zr - 1) .. (zr + 1)) {
              var vec = CELL_3D [Hash3D(m_seed, xi, yi, zi) and 255];

              var vecX = xi -x + vec.x;
              var vecY = yi -y + vec.y;
              var vecZ = zi -z + vec.z;

              var newDistance = Math . abs (vecX) + Math.abs(vecY) + Math.abs(vecZ);

              if (newDistance < distance) {
                distance = newDistance;
                xc = xi;
                yc = yi;
                zc = zi;
              }
            }
          }
        }
      }
      Natural -> {
        for (xi in (xr - 1) .. (xr + 1)) {
          for (yi in (yr - 1) .. (yr + 1)) {
            for (zi in (zr - 1) .. (zr + 1)) {
              var vec = CELL_3D [Hash3D(m_seed, xi, yi, zi) and 255];

              var vecX = xi -x + vec.x;
              var vecY = yi -y + vec.y;
              var vecZ = zi -z + vec.z;

              var newDistance =(Math.abs(vecX) + Math.abs(vecY) + Math.abs(vecZ)) + (vecX * vecX + vecY * vecY + vecZ * vecZ);

              if (newDistance < distance) {
                distance = newDistance;
                xc = xi;
                yc = yi;
                zc = zi;
              }
            }
          }
        }
      }
    }

    when (m_cellularReturnType) {
      CellValue -> return ValCoord3D(0, xc, yc, zc);
      NoiseLookup -> {
        var vec = CELL_3D [Hash3D(m_seed, xc, yc, zc) and 255];
        return m_cellularNoiseLookup.GetNoise(xc + vec.x, yc + vec.y, zc + vec.z);
      }
      Distance -> return distance - 1;
      else -> return 0f
    }
  }

  fun SingleCellular2Edge(x: Float, y: Float, z: Float): Float {
    var xr: Int = FastRound(x);
    var yr: Int = FastRound(y);
    var zr: Int = FastRound(z);

    var distance = 999999f;
    var distance2 = 999999f;

    when (m_cellularDistanceFunction) {
      Euclidean -> {
        for (xi in (xr - 1) .. (xr + 1)) {
          for (yi in (yr - 1) .. (yr + 1)) {
            for (zi in (zr - 1) .. (zr + 1)) {
              var vec = CELL_3D [Hash3D(m_seed, xi, yi, zi) and 255];

              var vecX = xi -x + vec.x;
              var vecY = yi -y + vec.y;
              var vecZ = zi -z + vec.z;

              var newDistance = vecX * vecX + vecY * vecY + vecZ * vecZ;

              distance2 = Math.max(Math.min(distance2, newDistance), distance);
              distance = Math.min(distance, newDistance);
            }
          }
        }
      }
      Manhattan -> {
        for (xi in (xr - 1) .. (xr + 1)) {
          for (yi in (yr - 1) .. (yr + 1)) {
            for (zi in (zr - 1) .. (zr + 1)) {
              var vec = CELL_3D [Hash3D(m_seed, xi, yi, zi) and 255];

              var vecX = xi -x + vec.x;
              var vecY = yi -y + vec.y;
              var vecZ = zi -z + vec.z;

              var newDistance = Math.abs (vecX) + Math.abs(vecY) + Math.abs(vecZ);

              distance2 = Math.max(Math.min(distance2, newDistance), distance);
              distance = Math.min(distance, newDistance);
            }
          }
        }
      }
      Natural -> {
        for (xi in (xr - 1) .. (xr + 1)) {
          for (yi in (yr - 1) .. (yr + 1)) {
            for (zi in (zr - 1) .. (zr + 1)) {
              var vec = CELL_3D [Hash3D(m_seed, xi, yi, zi) and 255];

              var vecX = xi -x + vec.x;
              var vecY = yi -y + vec.y;
              var vecZ = zi -z + vec.z;

              var newDistance =(Math.abs(vecX) + Math.abs(vecY) + Math.abs(vecZ)) + (vecX * vecX + vecY * vecY + vecZ * vecZ);

              distance2 = Math.max(Math.min(distance2, newDistance), distance);
              distance = Math.min(distance, newDistance);
            }
          }
        }
      }
      else -> {}
    }

    when (m_cellularReturnType) {
      Distance2 -> return distance2 - 1;
      Distance2Add -> return distance2 + distance - 1;
      Distance2Sub -> return distance2 - distance - 1;
      Distance2Mul -> return distance2 * distance - 1;
      Distance2Div -> return distance / distance2 - 1;
      else -> return 0f
    }
  }

  fun GetCellular(x1: Float, y1: Float): Float {

    var x = x1
    var y = y1

    x *= m_frequency;
    y *= m_frequency;

    when (m_cellularReturnType) {
      CellValue,NoiseLookup,Distance -> return SingleCellular(x, y);
      else -> return SingleCellular2Edge(x, y);
    }
  }

  fun SingleCellular(x: Float, y: Float): Float {
    var xr: Int = FastRound(x);
    var yr: Int = FastRound(y);

    var distance = 999999f;
    var xc: Int = 0
    var yc: Int = 0;

    when (m_cellularDistanceFunction) {
      Manhattan -> {
        for (xi in (xr - 1) .. (xr + 1)) {
          for (yi in (yr - 1) .. (yr + 1)) {
            var vec = CELL_2D [Hash2D(m_seed, xi, yi) and 255];

            var vecX = xi -x + vec.x;
            var vecY = yi -y + vec.y;

            var newDistance =(Math.abs(vecX) + Math.abs(vecY));

            if (newDistance < distance) {
              distance = newDistance;
              xc = xi;
              yc = yi;
            }
          }
        }
      }
      Natural -> {
        for (xi in (xr - 1) .. (xr + 1)) {
          for (yi in (yr - 1) .. (yr + 1)) {
            var vec = CELL_2D [Hash2D(m_seed, xi, yi) and 255];

            var vecX = xi -x + vec.x;
            var vecY = yi -y + vec.y;

            var newDistance =(Math.abs(vecX) + Math.abs(vecY)) + (vecX * vecX + vecY * vecY);

            if (newDistance < distance) {
              distance = newDistance;
              xc = xi;
              yc = yi;
            }
          }
        }
      }
      else -> {
        for (xi in (xr - 1) .. (xr + 1)) {
          for (yi in (yr - 1) .. (yr + 1)) {
            var vec = CELL_2D [Hash2D(m_seed, xi, yi) and 255];

            var vecX = xi -x + vec.x;
            var vecY = yi -y + vec.y;

            var newDistance = vecX * vecX +vecY * vecY;

            if (newDistance < distance) {
              distance = newDistance;
              xc = xi;
              yc = yi;
            }
          }
        }
      }
    }

    when (m_cellularReturnType) {
      CellValue -> return ValCoord2D(0, xc, yc);

      NoiseLookup -> {
        var vec = CELL_2D [Hash2D(m_seed, xc, yc) and 255];
        return m_cellularNoiseLookup.GetNoise(xc + vec.x, yc + vec.y);
      }
      Distance -> return distance - 1;
      else -> return 0f
    }
  }

  fun SingleCellular2Edge(x: Float, y: Float): Float {
    var xr: Int = FastRound(x);
    var yr: Int = FastRound(y);

    var distance = 999999f
    var distance2 = 999999f

    when (m_cellularDistanceFunction) {
      default,case Euclidean -> {
        for (xi in (xr - 1) .. (xr + 1)) {
          for (yi in (yr - 1) .. (yr + 1)) {
            var vec = CELL_2D [Hash2D(m_seed, xi, yi) and 255];

            var vecX = xi -x + vec.x;
            var vecY = yi -y + vec.y;

            var newDistance = vecX * vecX +vecY * vecY;

            distance2 = Math.max(Math.min(distance2, newDistance), distance);
            distance = Math.min(distance, newDistance);
          }
        }
      }
      Manhattan -> {
        for (xi in (xr - 1) .. (xr + 1)) {
          for (yi in (yr - 1) .. (yr + 1)) {
            var vec = CELL_2D [Hash2D(m_seed, xi, yi) and 255];

            var vecX = xi -x + vec.x;
            var vecY = yi -y + vec.y;

            var newDistance = Math . abs (vecX) + Math.abs(vecY);

            distance2 = Math.max(Math.min(distance2, newDistance), distance);
            distance = Math.min(distance, newDistance);
          }
        }
      }
      Natural -> {
        for (xi in (xr - 1) .. (xr + 1)) {
          for (yi in (yr - 1) .. (yr + 1)) {
            var vec = CELL_2D [Hash2D(m_seed, xi, yi) and 255];

            var vecX = xi -x + vec.x;
            var vecY = yi -y + vec.y;

            var newDistance =(Math.abs(vecX) + Math.abs(vecY)) + (vecX * vecX + vecY * vecY);

            distance2 = Math.max(Math.min(distance2, newDistance), distance);
            distance = Math.min(distance, newDistance);
          }
        }
      }
    }

    when (m_cellularReturnType) {
      Distance2 -> return distance2 - 1;
      Distance2Add -> return distance2 + distance - 1;
      Distance2Sub -> return distance2 - distance - 1;
      Distance2Mul -> return distance2 * distance - 1;
      Distance2Div -> return distance / distance2 - 1;
      else -> return 0f
    }
  }

  fun GradientPerturb(v3: Vector3f) {
    SingleGradientPerturb(m_seed, m_gradientPerturbAmp, m_frequency, v3);
  }

  fun GradientPerturbFractal(v3: Vector3f) {
    var seed = m_seed;
    var amp = m_gradientPerturbAmp * m_fractalBounding;
    var freq = m_frequency;

    SingleGradientPerturb(seed, amp, m_frequency, v3);

    for (i in 1 until m_octaves) {
      freq *= m_lacunarity;
      amp *= m_gain;
      SingleGradientPerturb(++seed, amp, freq, v3);
    }
  }

  fun SingleGradientPerturb(seed: Int, perturbAmp: Float, frequency: Float, v3: Vector3f) {
    var xf = v3.x * frequency;
    var yf = v3.y * frequency;
    var zf = v3.z * frequency;

    var x0: Int = FastFloor(xf);
    var y0: Int = FastFloor(yf);
    var z0: Int = FastFloor(zf);
    var x1: Int = x0 + 1;
    var y1: Int = y0 + 1;
    var z1: Int = z0 + 1;

    var xs = 0f
    var ys = 0f
    var zs = 0f

    when (m_interp) {
      Hermite -> {
        xs = InterpHermiteFunc(xf - x0);
        ys = InterpHermiteFunc(yf - y0);
        zs = InterpHermiteFunc(zf - z0);
      }
      Quintic -> {
        xs = InterpQuinticFunc(xf - x0);
        ys = InterpQuinticFunc(yf - y0);
        zs = InterpQuinticFunc(zf - z0);
      }
      else -> {
        xs = xf - x0;
        ys = yf - y0;
        zs = zf - z0;
      }
    }

    var vec0 = CELL_3D[Hash3D(seed, x0, y0, z0) and 255];
    var vec1 = CELL_3D[Hash3D(seed, x1, y0, z0) and 255];

    var lx0x = Lerp(vec0.x, vec1.x, xs);
    var ly0x = Lerp(vec0.y, vec1.y, xs);
    var lz0x = Lerp(vec0.z, vec1.z, xs);

    vec0 = CELL_3D[Hash3D(seed, x0, y1, z0) and 255];
    vec1 = CELL_3D[Hash3D(seed, x1, y1, z0) and 255];

    var lx1x = Lerp(vec0.x, vec1.x, xs);
    var ly1x = Lerp(vec0.y, vec1.y, xs);
    var lz1x = Lerp(vec0.z, vec1.z, xs);

    var lx0y = Lerp(lx0x, lx1x, ys);
    var ly0y = Lerp(ly0x, ly1x, ys);
    var lz0y = Lerp(lz0x, lz1x, ys);

    vec0 = CELL_3D[Hash3D(seed, x0, y0, z1) and 255];
    vec1 = CELL_3D[Hash3D(seed, x1, y0, z1) and 255];

    lx0x = Lerp(vec0.x, vec1.x, xs);
    ly0x = Lerp(vec0.y, vec1.y, xs);
    lz0x = Lerp(vec0.z, vec1.z, xs);

    vec0 = CELL_3D[Hash3D(seed, x0, y1, z1) and 255];
    vec1 = CELL_3D[Hash3D(seed, x1, y1, z1) and 255];

    lx1x = Lerp(vec0.x, vec1.x, xs);
    ly1x = Lerp(vec0.y, vec1.y, xs);
    lz1x = Lerp(vec0.z, vec1.z, xs);

    v3.x += Lerp(lx0y, Lerp(lx0x, lx1x, ys), zs) * perturbAmp;
    v3.y += Lerp(ly0y, Lerp(ly0x, ly1x, ys), zs) * perturbAmp;
    v3.z += Lerp(lz0y, Lerp(lz0x, lz1x, ys), zs) * perturbAmp;
  }

  fun GradientPerturb(v2: Vector2f) {
    SingleGradientPerturb(m_seed, m_gradientPerturbAmp, m_frequency, v2);
  }

  fun GradientPerturbFractal(v2: Vector2f) {
    var seed = m_seed;
    var amp = m_gradientPerturbAmp * m_fractalBounding;
    var freq = m_frequency;

    SingleGradientPerturb(seed, amp, m_frequency, v2);

    for (i in 1 until m_octaves) {
      freq *= m_lacunarity;
      amp *= m_gain;
      SingleGradientPerturb(++seed, amp, freq, v2);
    }
  }

  fun SingleGradientPerturb(seed: Int, perturbAmp: Float, frequency: Float, v2: Vector2f) {
    var xf = v2.x * frequency;
    var yf = v2.y * frequency;

    var x0: Int = FastFloor(xf);
    var y0: Int = FastFloor(yf);
    var x1: Int = x0 + 1;
    var y1: Int = y0 + 1;

    var xs = 0f
    var ys = 0f

    when (m_interp) {
      Hermite -> {
        xs = InterpHermiteFunc(xf - x0);
        ys = InterpHermiteFunc(yf - y0);
      }
      Quintic -> {
        xs = InterpQuinticFunc(xf - x0);
        ys = InterpQuinticFunc(yf - y0);
      }
      else -> {
        xs = xf - x0;
        ys = yf - y0;
      }
    }

    var vec0 = CELL_2D[Hash2D(seed, x0, y0) and 255];
    var vec1 = CELL_2D[Hash2D(seed, x1, y0) and 255];

    var lx0x = Lerp(vec0.x, vec1.x, xs);
    var ly0x = Lerp(vec0.y, vec1.y, xs);

    vec0 = CELL_2D[Hash2D(seed, x0, y1) and 255];
    vec1 = CELL_2D[Hash2D(seed, x1, y1) and 255];

    var lx1x = Lerp(vec0.x, vec1.x, xs);
    var ly1x = Lerp(vec0.y, vec1.y, xs);

    v2.x += Lerp(lx0x, lx1x, ys) * perturbAmp;
    v2.y += Lerp(ly0x, ly1x, ys) * perturbAmp;
  }

}
