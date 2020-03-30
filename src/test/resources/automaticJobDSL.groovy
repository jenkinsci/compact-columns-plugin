listView('automaticView') {
  columns {
    allStatusesColumn {
      colorblindHint 'underlinehint'
      onlyShowLastStatus false
      timeAgoTypeString 'PREFER_DATES'
      hideDays 7
    }
    jobNameColorColumn {
      showColor true
      showDescription true
      showLastBuild true
      colorblindHint 'nohint'
    }
    jobNameColumn()
    lastStableAndUnstableColumn()
    lastSuccessAndFailedColumn()
  }
}