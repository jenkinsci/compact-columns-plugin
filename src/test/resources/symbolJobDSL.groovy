listView('symbolView') {
  columns {
    compactAllStatuses {
      colorblindHint 'underlinehint'
      onlyShowLastStatus false
      timeAgoTypeString 'PREFER_DATES'
      hideDays 7
    }
    compactJobNameColor {
      showColor true
      showDescription true
      showLastBuild true
      colorblindHint 'nohint'
    }
    compactJobName()
    compactLastStableAndUnstable()
    compactLastSuccessAndFailed()
  }
}