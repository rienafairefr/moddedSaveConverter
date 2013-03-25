package com.rienafairefr.moddedSaveConverter;

import java.util.Arrays;
import java.util.Iterator;

public class StringDists {

	private static int minimum(int a, int b, int c) {
		return Math.min(Math.min(a, b), c);
	}

	public static float tokenizedcompare(String str1, String str2, String token){
		if (str1.equals(str2)){
			return 0f;
		}else{
			java.util.List<String> tokens1=Arrays.asList(str1.split(token));
			java.util.List<String> tokens2=Arrays.asList(str2.split(token));
			if (tokens1.size()>0){
				Iterator<String> it=tokens1.iterator();
				float dist=0;
				while (it.hasNext()){
					String tok=it.next();
					if (tokens2.contains(tok)){
						dist+=1f;
					}
				}
				return 1f-dist/tokens1.size();
			}else{
				if (str2.contains(str1)){
					return 0f;
				}else{
					return 1f;
				}
			}
		}
	}

	public static int computeLevenshteinDistance(CharSequence str1,
			CharSequence str2) {
		int[][] distance = new int[str1.length() + 1][str2.length() + 1];

		for (int i = 0; i <= str1.length(); i++)
			distance[i][0] = i;
		for (int j = 1; j <= str2.length(); j++)
			distance[0][j] = j;

		for (int i = 1; i <= str1.length(); i++)
			for (int j = 1; j <= str2.length(); j++)
				distance[i][j] = minimum(
						distance[i - 1][j] + 1,
						distance[i][j - 1] + 1,
						distance[i - 1][j - 1]
						                + ((str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0
						                		: 1));

		return distance[str1.length()][str2.length()];
	}

	public static int longestSubstr(String first, String second) {
		if (first == null || second == null || first.length() == 0 || second.length() == 0) {
			return 0;
		}

		int maxLen = 0;
		int fl = first.length();
		int sl = second.length();
		int[][] table = new int[fl][sl];

		for (int i = 0; i < fl; i++) {
			for (int j = 0; j < sl; j++) {
				if (first.charAt(i) == second.charAt(j)) {
					if (i == 0 || j == 0) {
						table[i][j] = 1;
					}
					else {
						table[i][j] = table[i - 1][j - 1] + 1;
					}
					if (table[i][j] > maxLen) {
						maxLen = table[i][j];
					}
				}
			}
		}
		return maxLen;
	}



}
