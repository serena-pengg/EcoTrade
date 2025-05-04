package com.example.Secondhand.service;

import com.example.Secondhand.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class RecommendationService {
    
    @Autowired
    private ProductService productService;
    
    private static final float ALPHA = 0.2f; // 环保权重调节系数
    private static final float MIN_CONFIDENCE = 0.6f; // 最小置信度阈值
    private static final float MIN_ECO_SCORE = 3.5f; // 最小环保评分阈值

    /**
     * 基于环保加权的关联规则推荐
     * @param transactionList 交易记录列表
     * @param targetProduct 目标商品
     * @return 推荐商品列表
     */
    public List<Product> getEcoWeightedRecommendations(List<Set<Long>> transactionList, Product targetProduct) {
        // 1. 计算频繁项集
        Map<Set<Long>, Double> frequentItemsets = findFrequentItemsets(transactionList);
        
        // 2. 生成关联规则并应用环保权重
        List<RecommendationRule> rules = generateWeightedRules(frequentItemsets, transactionList);
        
        // 3. 应用双重过滤机制
        List<RecommendationRule> filteredRules = applyDualFilter(rules);
        
        // 4. 获取推荐商品
        return getRecommendedProducts(filteredRules, targetProduct.getId());
    }

    /**
     * 计算加权支持度
     */
    private double calculateWeightedSupport(Set<Long> itemset, List<Set<Long>> transactions) {
        // 计算原始支持度
        double originalSupport = (double) transactions.stream()
                .filter(t -> t.containsAll(itemset))
                .count() / transactions.size();
        
        // 计算平均环保评分
        double avgEcoScore = itemset.stream()
                .map(productService::getProductById)
                .mapToDouble(p -> p.getEcoScore() != null ? p.getEcoScore() : 0)
                .average()
                .orElse(0.0);
        
        // 应用环保权重
        return originalSupport * (1 + ALPHA * avgEcoScore);
    }

    /**
     * 应用双重过滤机制
     */
    private List<RecommendationRule> applyDualFilter(List<RecommendationRule> rules) {
        return rules.stream()
                .filter(rule -> {
                    // 兴趣过滤：置信度过滤
                    boolean confidenceFilter = rule.getConfidence() >= MIN_CONFIDENCE;
                    
                    // 环保过滤：确保推荐结果包含至少一个高环保评分商品
                    boolean ecoFilter = rule.getConsequent().stream()
                            .map(productService::getProductById)
                            .anyMatch(p -> p.getEcoScore() != null && p.getEcoScore() >= MIN_ECO_SCORE);
                    
                    return confidenceFilter && ecoFilter;
                })
                .collect(Collectors.toList());
    }

    /**
     * 关联规则类
     */
    private static class RecommendationRule {
        private Set<Long> antecedent;
        private Set<Long> consequent;
        private double confidence;
        private double weightedSupport;

        public RecommendationRule(Set<Long> antecedent, Set<Long> consequent, 
                                double confidence, double weightedSupport) {
            this.antecedent = antecedent;
            this.consequent = consequent;
            this.confidence = confidence;
            this.weightedSupport = weightedSupport;
        }

        public Set<Long> getAntecedent() { return antecedent; }
        public Set<Long> getConsequent() { return consequent; }
        public double getConfidence() { return confidence; }
        public double getWeightedSupport() { return weightedSupport; }
    }

    /**
     * 生成加权关联规则
     */
    private List<RecommendationRule> generateWeightedRules(
            Map<Set<Long>, Double> frequentItemsets, List<Set<Long>> transactions) {
        List<RecommendationRule> rules = new ArrayList<>();
        
        for (Map.Entry<Set<Long>, Double> entry : frequentItemsets.entrySet()) {
            Set<Long> itemset = entry.getKey();
            if (itemset.size() < 2) continue;

            // 生成所有可能的规则
            for (Long item : itemset) {
                Set<Long> consequent = new HashSet<>();
                consequent.add(item);
                Set<Long> antecedent = new HashSet<>(itemset);
                antecedent.removeAll(consequent);

                // 计算加权置信度
                double weightedConfidence = calculateWeightedConfidence(
                    antecedent, consequent, transactions);

                rules.add(new RecommendationRule(
                    antecedent, consequent, weightedConfidence, entry.getValue()));
            }
        }
        
        return rules;
    }

    /**
     * 计算加权置信度
     */
    private double calculateWeightedConfidence(
            Set<Long> antecedent, Set<Long> consequent, List<Set<Long>> transactions) {
        double antecedentSupport = calculateWeightedSupport(antecedent, transactions);
        double ruleSupport = calculateWeightedSupport(
            Stream.concat(antecedent.stream(), consequent.stream())
                .collect(Collectors.toSet()), 
            transactions);
        
        return ruleSupport / antecedentSupport;
    }

    /**
     * 获取推荐商品列表
     */
    private List<Product> getRecommendedProducts(List<RecommendationRule> rules, Long targetProductId) {
        return rules.stream()
                .filter(rule -> rule.getAntecedent().contains(targetProductId))
                .flatMap(rule -> rule.getConsequent().stream())
                .distinct()
                .map(productService::getProductById)
                .filter(Objects::nonNull)
                .sorted((p1, p2) -> {
                    // 按环保评分降序排序
                    float score1 = p1.getEcoScore() != null ? p1.getEcoScore() : 0;
                    float score2 = p2.getEcoScore() != null ? p2.getEcoScore() : 0;
                    return Float.compare(score2, score1);
                })
                .collect(Collectors.toList());
    }

    /**
     * 查找频繁项集（使用Apriori算法）
     */
    private Map<Set<Long>, Double> findFrequentItemsets(List<Set<Long>> transactions) {
        Map<Set<Long>, Double> frequentItemsets = new HashMap<>();
        
        // 获取所有单个商品
        Set<Long> allItems = transactions.stream()
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
        
        // 生成候选1项集
        Map<Set<Long>, Double> candidates = new HashMap<>();
        for (Long item : allItems) {
            Set<Long> itemset = new HashSet<>();
            itemset.add(item);
            candidates.put(itemset, calculateWeightedSupport(itemset, transactions));
        }
        
        // 迭代生成更大的频繁项集
        while (!candidates.isEmpty()) {
            // 添加当前频繁项集
            frequentItemsets.putAll(candidates);
            
            // 生成下一级候选项集
            candidates = generateNextCandidates(candidates.keySet(), transactions);
        }
        
        return frequentItemsets;
    }

    /**
     * 生成下一级候选项集
     */
    private Map<Set<Long>, Double> generateNextCandidates(
            Set<Set<Long>> currentItemsets, List<Set<Long>> transactions) {
        Map<Set<Long>, Double> nextCandidates = new HashMap<>();
        
        // 合并当前频繁项集生成候选项集
        List<Set<Long>> itemsetList = new ArrayList<>(currentItemsets);
        for (int i = 0; i < itemsetList.size(); i++) {
            for (int j = i + 1; j < itemsetList.size(); j++) {
                Set<Long> candidate = new HashSet<>();
                candidate.addAll(itemsetList.get(i));
                candidate.addAll(itemsetList.get(j));
                
                if (candidate.size() == itemsetList.get(i).size() + 1) {
                    double support = calculateWeightedSupport(candidate, transactions);
                    nextCandidates.put(candidate, support);
                }
            }
        }
        
        return nextCandidates;
    }
} 