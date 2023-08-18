import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders

private const val PLATFORMS_ENDPOINT = "/api/v1/platforms"

fun MockMvc.countPlatforms() =
    this.perform(MockMvcRequestBuilders.get("$PLATFORMS_ENDPOINT/count").contentType(APPLICATION_JSON))

fun MockMvc.retrievePlatform(platformId: String) =
    this.perform(MockMvcRequestBuilders.get("$PLATFORMS_ENDPOINT/$platformId").contentType(APPLICATION_JSON))

fun MockMvc.retrieveAllPlatforms() =
    this.perform(MockMvcRequestBuilders.get(PLATFORMS_ENDPOINT).contentType(APPLICATION_JSON))

fun MockMvc.savePlatform(payload: String) = this.perform(
    MockMvcRequestBuilders.post(PLATFORMS_ENDPOINT).contentType(APPLICATION_JSON).content(payload)
)

fun MockMvc.updatePlatform(platformId: String, payload: String) = this.perform(
    MockMvcRequestBuilders.put("$PLATFORMS_ENDPOINT/$platformId").contentType(APPLICATION_JSON).content(payload)
)

fun MockMvc.deletePlatform(platformId: String) = this.perform(
    MockMvcRequestBuilders.delete("$PLATFORMS_ENDPOINT/$platformId").contentType(APPLICATION_JSON)
)