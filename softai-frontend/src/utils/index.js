// 生成8位随机数字的会话ID
export const generateSessionId = () => {
  // 生成0-99999999的随机数，不足8位补0
  const randomNum = Math.floor(Math.random() * 100000000);
  return randomNum.toString().padStart(8, '0');
};