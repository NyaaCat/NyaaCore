UPDATE test3
SET data1=(SELECT key FROM test4 WHERE test4.data1 = test3.data2)
WHERE test3.data2 IN (SELECT data1 FROM test4);