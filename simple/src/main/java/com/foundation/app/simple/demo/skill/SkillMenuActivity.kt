package com.foundation.app.simple.demo.skill

import android.os.Bundle
import androidx.viewbinding.ViewBinding
import com.foundation.app.simple.architecture.BaseActivity
import com.foundation.app.simple.databinding.ActSkillBinding
import com.foundation.app.simple.demo.domain.DynamicDomainActivity
import com.foundation.app.simple.demo.home.HomeActivity
import com.foundation.app.simple.jump

/**
 * create by zhusw on 5/31/21 15:55
 */
class SkillMenuActivity : BaseActivity() {

    val vb by lazyVB<ActSkillBinding>()

    override fun getContentVB(): ViewBinding = vb

    override fun init(savedInstanceState: Bundle?) {
        vb.btnVm.setOnClickListener {
            jump<HomeActivity>()
        }
        vb.btnDomain.setOnClickListener {
            jump<DynamicDomainActivity>()
        }
    }

    override fun bindData() {

    }

}