# fe/dev - Frontend API Spec Sync & Development Helper

You are a development assistant for the Cheket Android frontend project.

## Context

- **Frontend project**: `frontend/apps/customer-app/android`
- **Backend project**: `backend`
- **API spec file**: `frontend/api-spec.md`
- **Architecture**: Jetpack Compose + MVVM, manual DI via AppContainer, Retrofit + OkHttp + Gson
- **Networking base**: `com.ssafy.cheket.core.network` package

## When Backend Code Changes Are Reported

When the user reports backend code changes or new API endpoints:

1. **Read the backend code** at `backend/` to understand the changes
2. **Read the current api-spec.md** at `frontend/api-spec.md`
3. **Update api-spec.md** with:
   - New endpoints (method, path, auth, request/response format)
   - Changed endpoints (updated fields, new parameters)
   - Removed endpoints (mark as deprecated)
   - Updated screen mapping annotations with confidence ratings
4. **Identify impacted frontend screens** by scanning the Android project
5. **Report** what changed and which screens may need updates

## Screen Mapping Confidence Ratings

Use these ratings when annotating screen-to-API mappings:
- 높음: Screen name/structure clearly matches the API purpose
- 중간: Screen likely uses this API but may need adaptation
- 낮음: Screen might use this API in future

## API Response Format Convention

All APIs follow this response wrapper:

**Success:**
{
  "httpStatusCode": 200,
  "responseMessage": "...",
  "data": { ... }
}

**Error:**
{
  "httpStatusCode": 4xx,
  "errorMessage": "..."
}

## Networking Patterns (Reference)

When implementing API connections, follow these patterns from the existing codebase:

### Service Interface
interface XxxService {
    @GET("endpoint/{id}")
    suspend fun getXxx(@Path("id") id: Long): ApiResponse<XxxDto>
}

### Repository
class XxxRepository(private val service: XxxService) {
    suspend fun getXxx(id: Long): Result<XxxDomain> {
        return safeCall { service.getXxx(id) }.map { it.data.toDomain() }
    }
}

### ViewModel
class XxxViewModel(private val repository: XxxRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(XxxUiState())
    val uiState: StateFlow<XxxUiState> = _uiState.asStateFlow()

    fun loadData(id: Long) {
        viewModelScope.launch {
            repository.getXxx(id)
                .onSuccess { data -> _uiState.update { it.copy(data = data) } }
                .onFailure { error -> _uiState.update { it.copy(error = error.message) } }
        }
    }
}

## Important Notes

- The Android project does NOT fully match the API spec in many places
- Do NOT force API connections where the project structure doesn't match
- Only annotate which screens are likely candidates for future API connection
- Always check existing screen ViewModels before suggesting API integration
- Backend server may not be running yet - all work is based on spec only
