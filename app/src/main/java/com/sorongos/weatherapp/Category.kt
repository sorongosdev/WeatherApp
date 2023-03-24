package com.sorongos.weatherapp

import com.google.gson.annotations.SerializedName

/**원하는 카테고리만 받음, 다른 형태는 모두 null*/
enum class Category {
    @SerializedName("POP")
    POP, //강수확률
    @SerializedName("PTY")
    PTY, //강수형태
    @SerializedName("SKY")
    SKY, //하늘 상태
    @SerializedName("TMP")
    TMP, //1시간 기온
}