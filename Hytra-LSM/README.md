# Inverted_LSM

  本项目是基于 LSM_Tree 思想搭建的内外存混合存储的索引结构项目。数据将会存储在内存+外存。
  

## 编译构建可执行程序

1. 安装 Cmake

2. 使用 `cmake -S. -Bbuild` 命令编译本项目，生成 `build` 目录。

3. 使用 `cmake --build build` 命令生成可执行文件，在 `build` 目录下生成可执行文件。
   

## 调试运行

1. 在项目根路径创建文件夹 `index_file`。

2. 参考`CMakeLists.txt`文件的结构，一共 2 个可执行的程序，分别命名为 test_insert_operation 和 Inverted_LSM。

   * `test_insert_operation` 是将数据读取到 LSM_Tree 中并构建出索引结构的测试程序。

   * `Inverted_LSM` 是启动 socket 程序，将入口暴露到 localhost:9200 上提供给用户执行 query 使用的。可以参考 `client_test.py`的写法。

3. 在 IDE 中运行的时候请注意，项目的运行目录（Working Directory）需要设置为项目根目录，否则索引文件不会创建到指定的 `index_file` 路径下。

4. 在 ARM 架构机器上运行请反注释掉 `CMakeLists.txt` 文件中的 `set(CMAKE_SYSTEM_PROCESSOR arm)` 一行。


# 实现细节

test_insert_operation 是提供将现有的文件（`operation/put.config`）里的数据读取到 LSM_Tree 并存储在分级的索引结构中。
其中硬盘部分的数据放在了 `index_file` 目录下，在不断地插入数据过程中，会自动合并数据并存储到下一层。


# 传输协议

本项目使用 socket 作为通信方式。支持的语句使用命令式，具体包括插入（包括删除和更新）、获取、LSM 状态、更新新配置和断开连接等操作。


## 插入（PUT）

   插入数据使用 `put:[key],[value]` 的格式传入，解析时根据冒号和逗号的位置判断出键值并更新到 LSM 树上。放入的当日分片上的 LSM 树。
   返回OK/ERR，需要主动读取获取。


## 获取（GET）

   获取数据使用 `get:[date],[key]`  的格式传入，解析时同样按照冒号的位置判断键的位置并从 LSM 树上查询到数据并返回结果数组。获取到的是当天日期分片上的数据。
   返回一行具体的数值，用逗号分隔。


## 获取状态（STATUS）

   获取 LSM 树的状态使用 `status` 命令传入，底层会去获取到内存层和硬盘层分别去获取当前的数据的数量并按照 [level0]:[count0],[level1]:[count1]... 的格式返回给用户。
   返回一组存储状态，用逗号分隔。


## 更新配置（CONFIG）

   更新 LSM 树的配置信息使用 `config:[date],[path]`，由于 LSM 树是先读取配置生成特定结构的树再存入数据，因此更新配置则会将指定的时间片上的 LSM 树数据清空。需要结合 PUT 命令将数据再放入。
   返回OK/ERR，需要主动读取获取。

