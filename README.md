# app-rock common lib for all apps of Mariano (IN MARCH 2022)

<pre>Developers : MANKIRAT SINGH, Bhumika Sharma, ANKIT, Ashish David </pre>

# In project level build.gradle and setting.gradle

<pre>
allprojects {
    repositories {
    
        maven { url 'https://jitpack.io' }    
   }
}
</pre>

# In app level gradle

<pre>
dependencies {
    
    //app-rock
    implementation 'com.github.approckteam:app-rock:latest_version'
}
</pre>

# Usage

<pre>Please check sample BaseActivity and StartingActivity</pre>

<pre>
binding.btnShowInterstitial.setOnClickListener {
    adMobInter()
}

binding.flBannerAd.adMobBanner()

binding.flNativeAd.adMobNative()
</pre>