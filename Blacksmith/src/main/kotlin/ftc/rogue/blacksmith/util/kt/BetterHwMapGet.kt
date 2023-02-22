package ftc.rogue.blacksmith.util.kt

import com.qualcomm.robotcore.hardware.HardwareMap

/**
 * [Docs link](https://blacksmithftc.vercel.app/util/kt/better-hwmap-get)
 *
 * Syntactic sugar for [HardwareMap.get].
 *
 * __Note: This is Kotlin only.__
 *
 * Basically, due to type erasure with Java generics, when using [HardwareMap.get], you need to
 * manually pass in the class of the component you want to get. e.g.
 * ```
 * val motor = hardwareMap.get(DcMotorEx::class.java, "motor")
 * ```
 * Using reified types and operator overloading, we can simply do this instead:
 * ```
 * val motor = hardwareMap<DcMotorEx>("motor")
 * //or
 * val motor: DcMotorEx = hardwareMap("motor")
 * ```
 *
 * *Also, for why I did not simply use `get`, it's because the `get` name is already
 * taken by the `HardwareMap` class :(*
 *
 * @param T The type of the component you want to get.
 * @param name The name of the component you want to get.
 * @author KG
 */
inline operator fun <reified T> HardwareMap.invoke(name: String): T = get(T::class.java, name)
