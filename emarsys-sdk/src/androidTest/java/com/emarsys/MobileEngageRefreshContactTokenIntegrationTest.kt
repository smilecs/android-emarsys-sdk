package com.emarsys

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import com.emarsys.config.EmarsysConfig
import com.emarsys.core.activity.ActivityLifecycleWatchdog
import com.emarsys.core.activity.CurrentActivityWatchdog
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.device.LanguageProvider
import com.emarsys.core.di.DependencyContainer
import com.emarsys.core.di.DependencyInjection
import com.emarsys.core.di.getDependency
import com.emarsys.core.notification.NotificationManagerHelper
import com.emarsys.core.provider.hardwareid.HardwareIdProvider
import com.emarsys.core.provider.version.VersionProvider
import com.emarsys.core.storage.Storage
import com.emarsys.core.storage.StringStorage
import com.emarsys.di.DefaultEmarsysDependencyContainer
import com.emarsys.mobileengage.MobileEngageRefreshTokenInternal
import com.emarsys.mobileengage.RefreshTokenInternal
import com.emarsys.mobileengage.event.EventServiceInternal
import com.emarsys.mobileengage.storage.MobileEngageStorageKey
import com.emarsys.testUtil.*
import com.emarsys.testUtil.mockito.whenever
import com.emarsys.testUtil.rules.RetryRule
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.junit.*
import org.junit.rules.TestRule
import org.mockito.Mockito
import java.util.concurrent.CountDownLatch

class MobileEngageRefreshContactTokenIntegrationTest {

    companion object {
        private const val APP_ID = "14C19-A121F"
        private const val CONTACT_FIELD_ID = 3

        @BeforeClass
        @JvmStatic
        fun beforeAll() {
            val options: FirebaseOptions = FirebaseOptions.Builder()
                    .setApplicationId("com.emarsys.sdk")
                    .build()

            try {
                FirebaseApp.initializeApp(InstrumentationRegistry.getTargetContext(), options)
            } catch (ignored: java.lang.Exception) {

            }
        }

        @AfterClass
        @JvmStatic
        fun afterAll() {
            FirebaseApp.clearInstancesForTest()
        }
    }

    private lateinit var completionListenerLatch: CountDownLatch
    private lateinit var baseConfig: EmarsysConfig
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var contactTokenStorage: Storage<String?>

    private var errorCause: Throwable? = null

    private val application: Application
        get() = InstrumentationRegistry.getTargetContext().applicationContext as Application

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Rule
    @JvmField
    val retryRule: RetryRule = RetryUtils.retryRule

    @Before
    fun setup() {
        DatabaseTestUtils.deleteCoreDatabase()

        baseConfig = EmarsysConfig.Builder()
                .application(application)
                .mobileEngageApplicationCode(APP_ID)
                .contactFieldId(CONTACT_FIELD_ID)
                .build()

        FeatureTestUtils.resetFeatures()

        val setupLatch = CountDownLatch(1)
        DependencyInjection.setup(object : DefaultEmarsysDependencyContainer(baseConfig) {
            override fun getDeviceInfo(): DeviceInfo {
                return DeviceInfo(
                        application,
                        Mockito.mock(HardwareIdProvider::class.java).apply {
                            whenever(provideHardwareId()).thenReturn("mobileengage_integration_hwid")
                        },
                        Mockito.mock(VersionProvider::class.java).apply {
                            whenever(provideSdkVersion()).thenReturn("0.0.0-mobileengage_integration_version")
                        },
                        LanguageProvider(),
                        Mockito.mock(NotificationManagerHelper::class.java),
                        true
                )
            }
        })
        DependencyInjection.getContainer<DependencyContainer>().getCoreSdkHandler().post {
            setupLatch.countDown()
        }

        setupLatch.await()

        errorCause = null

        ConnectionTestUtils.checkConnection(application)

        sharedPreferences = application.getSharedPreferences("emarsys_shared_preferences", Context.MODE_PRIVATE)

        Emarsys.setup(baseConfig)

        contactTokenStorage = getDependency<StringStorage>(MobileEngageStorageKey.CONTACT_TOKEN.key)
        contactTokenStorage.remove()

        getDependency<StringStorage>(MobileEngageStorageKey.PUSH_TOKEN.key).remove()

        IntegrationTestUtils.doLogin()

        completionListenerLatch = CountDownLatch(1)
    }

    @After
    fun tearDown() {
        try {
            FeatureTestUtils.resetFeatures()

            getDependency<Handler>("coreSdkHandler").looper.quit()
            application.unregisterActivityLifecycleCallbacks(getDependency<ActivityLifecycleWatchdog>())
            application.unregisterActivityLifecycleCallbacks(getDependency<CurrentActivityWatchdog>())

            contactTokenStorage.remove()

            DependencyInjection.tearDown()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    @Test
    fun testRefreshContactToken() {
        contactTokenStorage.remove()

        val refreshTokenInternal = getDependency<RefreshTokenInternal>()

        refreshTokenInternal.refreshContactToken(this::eventuallyStoreResult).also(this::eventuallyAssertSuccess)

        contactTokenStorage.get() shouldNotBe null
    }

    @Test
    fun testRefreshContactToken_shouldUpdateContactToken_whenOutDated() {
        contactTokenStorage.remove()
        contactTokenStorage.set("tokenForIntegrationTest")

        val eventServiceInternal = getDependency<EventServiceInternal>("defaultInstance")

        eventServiceInternal.trackInternalCustomEvent("integrationTest", emptyMap(), this::eventuallyStoreResult).also(this::eventuallyAssertSuccess)

        contactTokenStorage.get() shouldNotBe "tokenForIntegrationTest"
    }

    private fun eventuallyStoreResult(errorCause: Throwable?) {
        this.errorCause = errorCause
        completionListenerLatch.countDown()
    }

    private fun eventuallyAssertSuccess(ignored: Any) {
        completionListenerLatch.await()
        errorCause shouldBe null
    }

}