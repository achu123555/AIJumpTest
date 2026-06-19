package com.achu.aijumptest.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 工具类
 *
 * 常用数据类型：
 * 1. String：字符串、计数器、验证码、token
 * 2. Hash：对象信息、用户信息
 * 3. List：消息队列、列表数据
 * 4. Set：去重集合、点赞用户集合
 * 5. ZSet：排行榜、积分榜、热度榜
 */

@Component
@RequiredArgsConstructor
public class RedisUtils {

    private final RedisTemplate<String, Object> redisTemplate;

    // ============================= 通用操作 =============================

    /**
     * 设置 key 的过期时间
     *
     * @param key  缓存键
     * @param time 时间，单位秒
     * @return 是否成功
     */
    public Boolean expire(String key, long time) {
        if (time <= 0) {
            return false;
        }
        return redisTemplate.expire(key, time, TimeUnit.SECONDS);
    }

    /**
     * 设置 key 的过期时间
     *
     * @param key      缓存键
     * @param time     时间
     * @param timeUnit 时间单位
     * @return 是否成功
     */
    public Boolean expire(String key, long time, TimeUnit timeUnit) {
        if (time <= 0) {
            return false;
        }
        return redisTemplate.expire(key, time, timeUnit);
    }

    /**
     * 获取 key 的剩余过期时间
     *
     * @param key 缓存键
     * @return 剩余时间，单位秒；-1 表示永久有效；-2 表示 key 不存在
     */
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 判断 key 是否存在
     *
     * @param key 缓存键
     * @return 是否存在
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 删除一个或多个 key
     *
     * @param keys 缓存键
     * @return 删除数量
     */
    public Long delete(String... keys) {
        if (keys == null || keys.length == 0) {
            return 0L;
        }
        return redisTemplate.delete(List.of(keys));
    }

    /**
     * 删除多个 key
     *
     * @param keys 缓存键集合
     * @return 删除数量
     */
    public Long delete(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return 0L;
        }
        return redisTemplate.delete(keys);
    }

    // ============================= String 操作 =============================

    /**
     * 获取普通缓存
     *
     * @param key 缓存键
     * @return 缓存值
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 获取普通缓存并转换类型
     *
     * @param key   缓存键
     * @param clazz 目标类型
     * @return 缓存值
     */
    public <T> T get(String key, Class<T> clazz) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        return clazz.cast(value);
    }

    /**
     * 设置普通缓存
     *
     * @param key   缓存键
     * @param value 缓存值
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 设置普通缓存并设置过期时间
     *
     * @param key   缓存键
     * @param value 缓存值
     * @param time  时间，单位秒
     */
    public void set(String key, Object value, long time) {
        if (time > 0) {
            redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
        } else {
            redisTemplate.opsForValue().set(key, value);
        }
    }

    /**
     * 设置普通缓存并设置过期时间
     *
     * @param key      缓存键
     * @param value    缓存值
     * @param time     时间
     * @param timeUnit 时间单位
     */
    public void set(String key, Object value, long time, TimeUnit timeUnit) {
        if (time > 0) {
            redisTemplate.opsForValue().set(key, value, time, timeUnit);
        } else {
            redisTemplate.opsForValue().set(key, value);
        }
    }

    /**
     * key 不存在时才设置
     *
     * 常用于简单分布式锁：
     * SET key value NX EX timeout
     *
     * @param key   缓存键
     * @param value 缓存值
     * @param time  过期时间，单位秒
     * @return 是否设置成功
     */
    public Boolean setIfAbsent(String key, Object value, long time) {
        return redisTemplate.opsForValue().setIfAbsent(key, value, time, TimeUnit.SECONDS);
    }

    /**
     * 递增
     *
     * @param key   缓存键
     * @param delta 增加数量
     * @return 增加后的值
     */
    public Long increment(String key, long delta) {
        if (delta < 0) {
            throw new IllegalArgumentException("递增因子必须大于等于 0");
        }
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 递减
     *
     * @param key   缓存键
     * @param delta 减少数量
     * @return 减少后的值
     */
    public Long decrement(String key, long delta) {
        if (delta < 0) {
            throw new IllegalArgumentException("递减因子必须大于等于 0");
        }
        return redisTemplate.opsForValue().decrement(key, delta);
    }

    // ============================= Hash 操作 =============================

    /**
     * 获取 Hash 中指定字段的值
     *
     * @param key  缓存键
     * @param item Hash 字段
     * @return 字段值
     */
    public Object hGet(String key, String item) {
        return redisTemplate.opsForHash().get(key, item);
    }

    /**
     * 获取整个 Hash
     *
     * @param key 缓存键
     * @return Hash 所有键值对
     */
    public Map<Object, Object> hGetAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 设置 Hash 字段
     *
     * @param key   缓存键
     * @param item  Hash 字段
     * @param value 字段值
     */
    public void hSet(String key, String item, Object value) {
        redisTemplate.opsForHash().put(key, item, value);
    }

    /**
     * 设置 Hash 字段并设置 key 过期时间
     *
     * @param key   缓存键
     * @param item  Hash 字段
     * @param value 字段值
     * @param time  过期时间，单位秒
     */
    public void hSet(String key, String item, Object value, long time) {
        redisTemplate.opsForHash().put(key, item, value);
        if (time > 0) {
            expire(key, time);
        }
    }

    /**
     * 批量设置 Hash
     *
     * @param key 缓存键
     * @param map Hash 键值对
     */
    public void hSetAll(String key, Map<String, Object> map) {
        redisTemplate.opsForHash().putAll(key, map);
    }

    /**
     * 批量设置 Hash 并设置过期时间
     *
     * @param key  缓存键
     * @param map  Hash 键值对
     * @param time 过期时间，单位秒
     */
    public void hSetAll(String key, Map<String, Object> map, long time) {
        redisTemplate.opsForHash().putAll(key, map);
        if (time > 0) {
            expire(key, time);
        }
    }

    /**
     * 删除 Hash 中的字段
     *
     * @param key   缓存键
     * @param items Hash 字段
     * @return 删除数量
     */
    public Long hDelete(String key, Object... items) {
        return redisTemplate.opsForHash().delete(key, items);
    }

    /**
     * 判断 Hash 中是否存在某字段
     *
     * @param key  缓存键
     * @param item Hash 字段
     * @return 是否存在
     */
    public Boolean hHasKey(String key, String item) {
        return redisTemplate.opsForHash().hasKey(key, item);
    }

    /**
     * Hash 字段递增
     *
     * @param key   缓存键
     * @param item  Hash 字段
     * @param delta 增加数量
     * @return 增加后的值
     */
    public Double hIncrement(String key, String item, double delta) {
        return redisTemplate.opsForHash().increment(key, item, delta);
    }

    /**
     * Hash 字段递减
     *
     * @param key   缓存键
     * @param item  Hash 字段
     * @param delta 减少数量
     * @return 减少后的值
     */
    public Double hDecrement(String key, String item, double delta) {
        return redisTemplate.opsForHash().increment(key, item, -delta);
    }

    // ============================= Set 操作 =============================

    /**
     * 获取 Set 中所有元素
     *
     * @param key 缓存键
     * @return Set 集合
     */
    public Set<Object> sMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * 判断 value 是否在 Set 中
     *
     * @param key   缓存键
     * @param value 值
     * @return 是否存在
     */
    public Boolean sIsMember(String key, Object value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    /**
     * 向 Set 中添加元素
     *
     * @param key    缓存键
     * @param values 值
     * @return 添加成功数量
     */
    public Long sAdd(String key, Object... values) {
        return redisTemplate.opsForSet().add(key, values);
    }

    /**
     * 向 Set 中添加元素并设置过期时间
     *
     * @param key    缓存键
     * @param time   过期时间，单位秒
     * @param values 值
     * @return 添加成功数量
     */
    public Long sAdd(String key, long time, Object... values) {
        Long count = redisTemplate.opsForSet().add(key, values);
        if (time > 0) {
            expire(key, time);
        }
        return count;
    }

    /**
     * 获取 Set 大小
     *
     * @param key 缓存键
     * @return 元素数量
     */
    public Long sSize(String key) {
        return redisTemplate.opsForSet().size(key);
    }

    /**
     * 删除 Set 中的元素
     *
     * @param key    缓存键
     * @param values 值
     * @return 删除数量
     */
    public Long sRemove(String key, Object... values) {
        return redisTemplate.opsForSet().remove(key, values);
    }

    // ============================= List 操作 =============================

    /**
     * 获取 List 指定范围元素
     *
     * @param key   缓存键
     * @param start 开始位置
     * @param end   结束位置，-1 表示所有
     * @return List 集合
     */
    public List<Object> lRange(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    /**
     * 获取 List 大小
     *
     * @param key 缓存键
     * @return List 长度
     */
    public Long lSize(String key) {
        return redisTemplate.opsForList().size(key);
    }

    /**
     * 根据索引获取 List 中的元素
     *
     * @param key   缓存键
     * @param index 索引
     * @return 元素
     */
    public Object lIndex(String key, long index) {
        return redisTemplate.opsForList().index(key, index);
    }

    /**
     * 向 List 右侧添加一个元素
     *
     * @param key   缓存键
     * @param value 值
     * @return 添加后长度
     */
    public Long lRightPush(String key, Object value) {
        return redisTemplate.opsForList().rightPush(key, value);
    }

    /**
     * 向 List 右侧添加多个元素
     *
     * @param key    缓存键
     * @param values 值集合
     * @return 添加后长度
     */
    public Long lRightPushAll(String key, Collection<Object> values) {
        return redisTemplate.opsForList().rightPushAll(key, values);
    }

    /**
     * 向 List 左侧添加一个元素
     *
     * @param key   缓存键
     * @param value 值
     * @return 添加后长度
     */
    public Long lLeftPush(String key, Object value) {
        return redisTemplate.opsForList().leftPush(key, value);
    }

    /**
     * 根据索引修改 List 中的元素
     *
     * @param key   缓存键
     * @param index 索引
     * @param value 新值
     */
    public void lSet(String key, long index, Object value) {
        redisTemplate.opsForList().set(key, index, value);
    }

    /**
     * 删除 List 中 N 个值为 value 的元素
     *
     * @param key   缓存键
     * @param count 删除数量，0 表示删除所有匹配值
     * @param value 值
     * @return 删除数量
     */
    public Long lRemove(String key, long count, Object value) {
        return redisTemplate.opsForList().remove(key, count, value);
    }

    /**
     * 从 List 左侧弹出元素
     *
     * @param key 缓存键
     * @return 弹出的元素
     */
    public Object lLeftPop(String key) {
        return redisTemplate.opsForList().leftPop(key);
    }

    /**
     * 从 List 右侧弹出元素
     *
     * @param key 缓存键
     * @return 弹出的元素
     */
    public Object lRightPop(String key) {
        return redisTemplate.opsForList().rightPop(key);
    }

    // ============================= ZSet 操作 =============================

    /**
     * 向有序集合中添加元素
     *
     * @param key   缓存键
     * @param value 值
     * @param score 分数
     * @return 是否成功
     */
    public Boolean zAdd(String key, Object value, double score) {
        return redisTemplate.opsForZSet().add(key, value, score);
    }

    /**
     * 增加有序集合中元素的分数
     *
     * @param key   缓存键
     * @param value 值
     * @param delta 增加的分数
     * @return 新的分数
     */
    public Double zIncrementScore(String key, Object value, double delta) {
        return redisTemplate.opsForZSet().incrementScore(key, value, delta);
    }

    /**
     * 获取有序集合中元素的分数
     *
     * @param key   缓存键
     * @param value 值
     * @return 分数
     */
    public Double zScore(String key, Object value) {
        return redisTemplate.opsForZSet().score(key, value);
    }

    /**
     * 获取有序集合中指定范围的元素，按分数从小到大
     *
     * @param key   缓存键
     * @param start 开始位置
     * @param end   结束位置
     * @return 元素集合
     */
    public Set<Object> zRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().range(key, start, end);
    }

    /**
     * 获取有序集合中指定范围的元素，按分数从大到小
     *
     * @param key   缓存键
     * @param start 开始位置
     * @param end   结束位置
     * @return 元素集合
     */
    public Set<Object> zReverseRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().reverseRange(key, start, end);
    }

    /**
     * 获取有序集合中指定范围的元素和分数，按分数从小到大
     *
     * @param key   缓存键
     * @param start 开始位置
     * @param end   结束位置
     * @return 元素和分数
     */
    public Set<ZSetOperations.TypedTuple<Object>> zRangeWithScores(String key, long start, long end) {
        return redisTemplate.opsForZSet().rangeWithScores(key, start, end);
    }

    /**
     * 获取有序集合中指定范围的元素和分数，按分数从大到小
     *
     * @param key   缓存键
     * @param start 开始位置
     * @param end   结束位置
     * @return 元素和分数
     */
    public Set<ZSetOperations.TypedTuple<Object>> zReverseRangeWithScores(String key, long start, long end) {
        return redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
    }

    /**
     * 根据分数范围获取元素
     *
     * @param key 缓存键
     * @param min 最小分数
     * @param max 最大分数
     * @return 元素集合
     */
    public Set<Object> zRangeByScore(String key, double min, double max) {
        return redisTemplate.opsForZSet().rangeByScore(key, min, max);
    }

    /**
     * 根据分数范围获取元素和分数
     *
     * @param key 缓存键
     * @param min 最小分数
     * @param max 最大分数
     * @return 元素和分数
     */
    public Set<ZSetOperations.TypedTuple<Object>> zRangeByScoreWithScores(String key, double min, double max) {
        return redisTemplate.opsForZSet().rangeByScoreWithScores(key, min, max);
    }

    /**
     * 获取有序集合中元素数量
     *
     * @param key 缓存键
     * @return 元素数量
     */
    public Long zSize(String key) {
        return redisTemplate.opsForZSet().size(key);
    }

    /**
     * 获取有序集合中元素排名，按分数从小到大
     *
     * @param key   缓存键
     * @param value 值
     * @return 排名，从 0 开始
     */
    public Long zRank(String key, Object value) {
        return redisTemplate.opsForZSet().rank(key, value);
    }

    /**
     * 获取有序集合中元素排名，按分数从大到小
     *
     * @param key   缓存键
     * @param value 值
     * @return 排名，从 0 开始
     */
    public Long zReverseRank(String key, Object value) {
        return redisTemplate.opsForZSet().reverseRank(key, value);
    }

    /**
     * 删除有序集合中的元素
     *
     * @param key    缓存键
     * @param values 值
     * @return 删除数量
     */
    public Long zRemove(String key, Object... values) {
        return redisTemplate.opsForZSet().remove(key, values);
    }

    /**
     * 删除指定排名范围内的元素
     *
     * @param key   缓存键
     * @param start 开始排名
     * @param end   结束排名
     * @return 删除数量
     */
    public Long zRemoveRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().removeRange(key, start, end);
    }

    /**
     * 删除指定分数范围内的元素
     *
     * @param key 缓存键
     * @param min 最小分数
     * @param max 最大分数
     * @return 删除数量
     */
    public Long zRemoveRangeByScore(String key, double min, double max) {
        return redisTemplate.opsForZSet().removeRangeByScore(key, min, max);
    }
}