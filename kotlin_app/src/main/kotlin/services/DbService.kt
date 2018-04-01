package services

import org.apache.commons.dbcp2.BasicDataSource
import org.springframework.jdbc.core.JdbcTemplate
import javax.sql.DataSource

object DbService {

    enum class Target(
        val username: String,
        val password: String,
        val host: String,
        val port: Int,
        val database: String
    ) {
        LOCAL(
            username = "manu",
            host = "localhost",
            port = 5432,
            password = "",
            database = "tv_shows_calendar"
        ),
        // This params may rotate periodically, check heroku to get the latest one
        HEROKU(
            username = "fvivwakvmsirvy".reversed(),
            host = "moc.swanozama.etupmoc.1-tsew-ue.46-622-57-45-2ce".reversed(),
            port = 5432,
            password = "be117185edaf16b6bf7c18c9d7c7db3b4457987e507d7cb47f3e58c9c4efae84".reversed(),
            database = "265aagb23odbed".reversed()
        )
    }


    private fun buildDataSource(target: Target): DataSource {
        val dataSource = BasicDataSource()
        dataSource.setDriverClassName("org.postgresql.Driver")
        dataSource.setUsername(target.username)
        dataSource.setPassword(target.password)
        dataSource.setUrl("jdbc:postgresql://${target.host}:${target.port}/${target.database}")
        dataSource.setValidationQuery("SELECT 1")
        return dataSource
    }

    fun deleteAllJsonData(): Unit {
        val jdbcTemplate = JdbcTemplate(buildDataSource(Target.LOCAL))
        jdbcTemplate.execute("TRUNCATE raw_json_data")
    }

    fun insertJson(target: Target, jsonStr: String): Unit {
        val jdbcTemplate = JdbcTemplate(buildDataSource(target))
        jdbcTemplate.update("INSERT INTO raw_json_data (content) VALUES (?)", jsonStr)
    }

}
