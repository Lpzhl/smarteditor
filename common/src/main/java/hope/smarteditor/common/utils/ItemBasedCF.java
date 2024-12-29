package hope.smarteditor.common.utils;

import java.util.*;

public class ItemBasedCF {

    // 计算物品相似度矩阵

    /**
     * 用户-物品评分矩阵：首先需要构建一个用户对物品的评分矩阵，矩阵的行表示用户，列表示物品，
     * 矩阵中的值代表用户对物品的评分（如1-5星），如果用户未评分某物品，则该位置为空或为零
     * @param ratings
     * @return
     */
    public static double[][] computeSimilarityMatrix(double[][] ratings) {
        int itemCount = ratings[0].length;
        double[][] similarityMatrix = new double[itemCount][itemCount];

        for (int i = 0; i < itemCount; i++) {
            for (int j = 0; j < itemCount; j++) {
                if (i != j) {
                    similarityMatrix[i][j] = cosineSimilarity(ratings, i, j);
                } else {
                    similarityMatrix[i][j] = 1.0;
                }
            }
        }

        return similarityMatrix;
    }

    // 计算余弦相似度

    /**
     * 选择相似度度量方法：常用的相似度度量方法包括余弦相似度、皮尔逊相关系数和调整的余弦相似度等。
     * 构建相似物品列表
     * 选择邻居物品：对于每个物品，选择与其相似度最高的
     * 𝑁
     * N 个物品作为其“邻居”。这个
     * 𝑁
     * N 是一个预先设定的参数，通常在10到50之间。
     * 预测用户对物品的评分
     * @param ratings
     * @param item1
     * @param item2
     * @return
     */
    private static double cosineSimilarity(double[][] ratings, int item1, int item2) {
        double numerator = 0.0;
        double denominator1 = 0.0;
        double denominator2 = 0.0;

        for (double[] userRatings : ratings) {
            double rating1 = userRatings[item1];
            double rating2 = userRatings[item2];

            if (rating1 > 0 && rating2 > 0) {
                numerator += rating1 * rating2;
                denominator1 += Math.pow(rating1, 2);
                denominator2 += Math.pow(rating2, 2);
            }
        }

        if (denominator1 == 0 || denominator2 == 0) {
            return 0.0;
        }

        return numerator / (Math.sqrt(denominator1) * Math.sqrt(denominator2));
    }

    // 基于物品的协同过滤推荐

    /**
     * 生成推荐列表
     * 筛选未评分物品：对于用户
     * u，筛选出用户尚未评分的物品，使用上述预测公式计算每个物品的预测评分。
     *
     * 排序与推荐：将这些物品按预测评分从高到低排序，选择前
     * K 个物品作为推荐列表
     * @param ratings
     * @param similarityMatrix
     * @param userIndex
     * @return
     */
    public static double[] recommend(double[][] ratings, double[][] similarityMatrix, int userIndex) {
        int itemCount = ratings[0].length;
        double[] scores = new double[itemCount];
        double[] userRatings = ratings[userIndex];

        for (int item = 0; item < itemCount; item++) {
            if (userRatings[item] == 0) { // 只推荐用户未评分的物品
                double score = 0.0;
                double similaritySum = 0.0;

                for (int otherItem = 0; otherItem < itemCount; otherItem++) {
                    if (userRatings[otherItem] > 0) {
                        score += similarityMatrix[item][otherItem] * userRatings[otherItem];
                        similaritySum += Math.abs(similarityMatrix[item][otherItem]);
                    }
                }

                if (similaritySum > 0) {
                    scores[item] = score / similaritySum;
                }
            }
        }

        return scores;
    }

/*    public static void main(String[] args) {
        // 示例评分矩阵，行表示用户，列表示物品
        // 0 表示用户未评分
        double[][] ratings = {
            {4, 0, 0, 5, 1, 0, 0},
            {5, 5, 4, 0, 0, 0, 0},
            {0, 0, 0, 2, 4, 5, 0},
            {0, 3, 0, 0, 0, 0, 3}
        };

        // 计算物品相似度矩阵
        double[][] similarityMatrix = computeSimilarityMatrix(ratings);

        // 为用户 0 推荐物品
        int userIndex = 0;
        double[] recommendations = recommend(ratings, similarityMatrix, userIndex);

        System.out.println("推荐分数：");
        for (int i = 0; i < recommendations.length; i++) {
            System.out.printf("物品 %d: %.2f\n", i, recommendations[i]);
        }
    }*/
}
