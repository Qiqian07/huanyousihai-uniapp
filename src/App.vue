<script>
import { applyTabBarTheme } from '@/utils/theme.js'

export default {
  onLaunch() {
    // 仅 H5：支持 ?theme=dark / ?theme=light 预览夜间稿（小程序端跟随系统）
    // #ifdef H5
    try {
      const q = new URLSearchParams(location.search).get('theme')
      if (q) document.documentElement.setAttribute('data-theme', q)
    } catch (e) {}
    // #endif
    // tabBar 为原生组件，需在挂载后同步主题
    setTimeout(applyTabBarTheme, 60)
  },
  onShow() {
    setTimeout(applyTabBarTheme, 60)
  },
  onHide() {}
}
</script>

<style lang="scss">
@import '@/styles/common.scss';
</style>
