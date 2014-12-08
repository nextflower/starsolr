package com.sysc.solrServer.similarity;

import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.search.DefaultSimilarity;

public class DuplicateCheckSimilarity extends DefaultSimilarity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2961634428895871191L;
	
	

	@Override
	public float computeNorm(String field, FieldInvertState state) {
		return 1.0f;
	}

	@Override
	public float queryNorm(float sumOfSquaredWeights) {
		return 1.0f;
	}

	@Override
	public float tf(float freq) {
		return 1.0f;
	}

	@Override
	public float sloppyFreq(int distance) {
		return 1.0f;
	}

	@Override
	public float idf(int docFreq, int numDocs) {
		return 1.0f;
	}

	@Override
	public float coord(int overlap, int maxOverlap) {
		return super.coord(overlap, maxOverlap) * 1000;
	}

}
