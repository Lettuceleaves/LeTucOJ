
local bucketKey = KEYS[1]
local submitKey = KEYS[2]
local lang = ARGV[1]
local taskStatus = ARGV[2]

if redis.call('EXISTS', submitKey) == 1 then
	return -7 -- 重复提交
end
redis.call('SET', submitKey, taskStatus)

local mem_str = redis.call('HGET', 'lang:mem', lang)
if not mem_str then
	redis.call('DEL', submitKey)
	return -1 -- 语言内存配置缺失
end


local mem = tonumber(mem_str)
if not mem or mem <= 0 then
	return -2 -- 语言内存配置错误
end


-- 2. 令牌桶限流（修复HMGET使用方式）
local rate = 100
local burst = 200
local time_result = redis.call('TIME')
local now = tonumber(time_result[1])

-- 正确获取哈希字段值
local bucket_data = redis.call('HMGET', bucketKey, 'last', 'stock')
if bucket_data[1] == false or bucket_data[2] == false then
	redis.call('HMSET', bucketKey, 'last', now, 'stock', burst - 1)
	redis.call('EXPIRE', bucketKey, 3600)
else
	local last = tonumber(bucket_data[1]) or now
	local stock = tonumber(bucket_data[2]) or burst

	-- 计算新令牌数
	local time_passed = math.max(0, now - last)
	stock = math.min(burst, stock + time_passed * rate)

	if stock < 1 then
		return -3 -- 令牌桶空
	end

	stock = stock - 1
	redis.call('HMSET', bucketKey, 'last', now, 'stock', stock)
	redis.call('EXPIRE', bucketKey, 3600)
end

-- 3. 获取最大内存配置（优化错误处理）
local maxBytes
local cached_max = redis.call('HGET', 'lang:mem', "total")
if cached_max then
	maxBytes = tonumber(cached_max)
else
	redis.call('DEL', submitKey)
	return -4 -- 最大内存配置缺失
end

if not maxBytes or maxBytes <= 0 then
	return -5 -- 最大内存配置错误
end

-- 4. 内存检查（使用原子操作避免并发问题）
local used_key = 'used_memory'
local current_used = tonumber(redis.call('GET', used_key) or '0')

if current_used + mem > maxBytes then
	return -6 -- 内存超限
end

-- 5. 原子性增加内存使用量
redis.call('INCRBY', used_key, mem)

return mem