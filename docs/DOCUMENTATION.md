### Cast Button Visibility Logic

The Cast button visibility is controlled by:

1. Single check if Cast is supported on the device
2. Button is always shown if Cast is supported, hidden if not
3. Default visibility is "gone" in layout

```kotlin
override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    // initialize cast button
    castButton = layout.rootView.findViewById(R.id.cast)
    castButton.setRemoteIndicatorDrawable(AppCompatResources.getDrawable(activity as Context, R.drawable.selector_cast_button))
    CastButtonFactory.setUpMediaRouteButton(activity as Context, castButton)
    
    // Show button if Cast is supported
    try {
        CastContext.getSharedInstance(activity as Context)
        changeCastButtonVisibility(true)
        Log.d(TAG, "Cast is supported, showing button")
    } catch (e: Exception) {
        Log.d(TAG, "Cast is not supported on this device")
        changeCastButtonVisibility(false)
    }
}
```

## Known Issues and Solutions

1. Cast Button Visibility - RESOLVED

- Previous issue: CastStateListener was unreliable in detecting available Cast devices
- Solution implemented:
    - Cast button is now always visible when Cast is supported on the device
    - Removes dependency on unreliable CastStateListener
    - Follows standard behavior of other Cast-enabled apps
    - Let's Cast framework handle device discovery and availability

2. Cast Device Detection

- Cast framework handles device discovery internally
- Button visibility no longer depends on device detection state
- More reliable user experience matching other Cast-enabled apps

## Development Notes

- Cast implementation simplified by removing dependency on CastStateListener
- Cast button visibility now determined solely by Cast support on device
- SwappablePlayer architecture allows seamless transition between playback modes

## Future Considerations

- Monitor for Cast framework updates that might improve device detection
- Consider adding optional detailed Cast state logging for debugging
- Document any issues found with this simplified approach