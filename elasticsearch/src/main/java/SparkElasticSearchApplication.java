import com.google.common.collect.ImmutableList;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.elasticsearch.spark.rdd.api.java.JavaEsSpark;

import static org.apache.spark.sql.functions.asc;

public class SparkElasticSearchApplication {
  public static void main(String[] args) {

    SparkSession spark = SparkSession.builder()
            .appName("Attendance by Category")
            .config("spark.master", "local")
            .config("spark.testing.memory", "2147480000")
            .config("spark.es.nodes", "elasticsearch")
            .config("spark.es.port", "9200")
            .config("spark.es.nodes.wan.only", "true")
            .getOrCreate();

    spark.sparkContext().hadoopConfiguration().set("fs.hdfs.impl",
            org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());

    spark.sparkContext().hadoopConfiguration().set("fs.file.impl",
            org.apache.hadoop.fs.LocalFileSystem.class.getName());

    JavaSparkContext javaSparkContext = JavaSparkContext
            .fromSparkContext(spark.sparkContext());

    Dataset<Row> data = spark.read()
            .format("com.databricks.spark.csv")
            .option("header", "false")
            .load(args[0])
            .toDF("year", "category", "attendance");

    data = data.withColumn("attendance",
            data.col("attendance")
                    .cast(DataTypes.IntegerType))
            .drop("year");

    data = data.groupBy("category")
            .sum("attendance")
            .orderBy(asc("category"));

    // WRITE TO ES
    JavaRDD<String> rdd = javaSparkContext.parallelize(
            ImmutableList.copyOf(data.toJSON().collectAsList()));
    JavaEsSpark.saveJsonToEs(rdd,"attendance/category");

    // READ FROM ES
    JavaRDD<String> esRDD = JavaEsSpark.esJsonRDD(javaSparkContext, "attendance/category").values();
    System.out.println(esRDD.collect());
  }
}
