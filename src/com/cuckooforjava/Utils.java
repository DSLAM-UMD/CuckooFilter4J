package com.cuckooforjava;

import java.io.Serializable;
import java.math.RoundingMode;
import java.util.Objects;

import javax.annotation.Nullable;

import com.google.common.math.DoubleMath;

public class Utils {

	/**
	 * The hashing algorithm used internally.
	 * 
	 * @author Mark Gunlogson
	 *
	 */
	public enum Algorithm {
		/**
		 * Murmer3 - 32 bit version, This is the default.
		 */
		Murmur3_32(0),
		/**
		 * Murmer3 - 128 bit version. Slower than 32 bit Murmer3, not sure why
		 * you would want to use this.
		 */
		Murmur3_128(1),
		/**
		 * SHA1 secure hash.
		 */
		sha256(2),
		/**
		 * SipHash(2,4) secure hash.
		 */
		sipHash24(3);
		private final int id;

		Algorithm(int id) {
			this.id = id;
		}

		public int getValue() {
			return id;
		}
	}

	/**
	 * when the filter becomes completely full, the last item that fails to be
	 * repositioned will be left without a home. We need to store it to avoid a
	 * false negative. Note that we use copy on write here since reads are more
	 * common than writes.
	 */
	static class Victim implements Serializable {
		private static final long serialVersionUID = -984233593241086192L;
		private long i1;
		private long i2;
		private long tag;

		Victim()
		{
		}
		Victim(long bucketIndex, long altIndex, long tag) {
			this.i1 = bucketIndex;
			this.i2 = altIndex;
			this.tag = tag;
		}

		public long getI1() {
			return i1;
		}

		public void setI1(long i1) {
			this.i1 = i1;
		}

		public long getI2() {
			return i2;
		}

		public void setI2(long i2) {
			this.i2 = i2;
		}

		public long getTag() {
			return tag;
		}

		public void setTag(long tag) {
			this.tag = tag;
		}

		@Override
		public int hashCode() {
			return Objects.hash(i1, i2, tag);
		}

		@Override
		public boolean equals(@Nullable Object object) {
			if (object == this) {
				return true;
			}
			if (object instanceof Utils.Victim) {
				Utils.Victim that = (Utils.Victim) object;
				return (this.i1 == that.i1 || this.i1 == that.i2) && this.tag == that.tag;
			}
			return false;
		}

		Victim copy() {
			return new Victim(i1, i2, tag);
		}
	}
	/**
	 * Calculates how many bits are needed to reach a given false positive rate.
	 * 
	 * @param fpProb
	 *            the false positive probability.
	 * @return the length of the tag needed (in bits) to reach the false
	 *         positive rate.
	 */
	public static int getBitsPerItemForFpRate(double fpProb,double loadFactor) {
		/*
		 * equation from Cuckoo Filter: Practically Better Than Bloom Bin Fan,
		 * David G. Andersen, Michael Kaminsky , Michael D. Mitzenmacher
		 */
		return DoubleMath.roundToInt(DoubleMath.log2((1 / fpProb) + 3) / loadFactor, RoundingMode.UP);
	}

	/**
	 * Calculates how many buckets are needed to hold the chosen number of keys,
	 * taking the standard load factor into account.
	 * 
	 * @param maxKeys
	 *            the number of keys the filter is expected to hold before
	 *            insertion failure.
	 * @return The number of buckets needed
	 */
	public static long getBucketsNeeded(long maxKeys,double loadFactor,int bucketSize) {
		/*
		 * force a power-of-two bucket count so hash functions for bucket index
		 * can hashBits%numBuckets and get randomly distributed index. See wiki
		 * "Modulo Bias". Only time we can get perfectly distributed index is
		 * when numBuckets is a power of 2.
		 */
		long bucketsNeeded = DoubleMath.roundToLong((1.0 / loadFactor) * maxKeys / bucketSize, RoundingMode.UP);
		// get next biggest power of 2
		long bitPos = Long.highestOneBit(bucketsNeeded);
		if (bucketsNeeded > bitPos)
			bitPos = bitPos << 1;
		return bitPos;
	}
	
	

}